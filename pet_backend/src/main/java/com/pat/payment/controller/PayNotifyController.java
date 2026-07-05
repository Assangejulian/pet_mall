package com.pat.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.pat.common.domain.Result;
import com.pat.payment.config.AlipayConfig;
import com.pat.payment.domain.dto.PaymentNotify;
import com.pat.payment.service.PaymentCallback;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方支付回调通知控制器。
 *
 * <p>接收支付宝/微信的异步通知，验签后通过 PaymentCallback 回调更新订单状态。</p>
 *
 * <h3>注意</h3>
 * <ul>
 *   <li><b>不要靠同步 returnUrl 判断支付成功</b>，必须以异步 notify 回调为准</li>
 *   <li>支付宝回调会多次重试，处理成功必须返回 "success"</li>
 *   <li>本地 localhost 无法接收回调，需用内网穿透（如 cpolar）暴露公网地址作为 notifyUrl</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/notify")
@Tag(name = "支付回调", description = "第三方支付平台回调通知")
public class PayNotifyController {

    private final AlipayConfig alipayConfig;
    private final com.pat.payment.config.WechatPayConfig wechatPayConfig;
    private final PaymentCallback paymentCallback;

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String TRADE_FINISHED = "TRADE_FINISHED";

    public PayNotifyController(AlipayConfig alipayConfig,
                               com.pat.payment.config.WechatPayConfig wechatPayConfig,
                               PaymentCallback paymentCallback) {
        this.alipayConfig = alipayConfig;
        this.wechatPayConfig = wechatPayConfig;
        this.paymentCallback = paymentCallback;
    }

    @Operation(summary = "模拟支付成功回调（开发测试用）")
    @PostMapping("/pay-success")
    public Result<Void> paySuccess(@RequestBody PaymentNotify notify) {
        log.info("模拟支付回调, 订单号 {}", notify.getOutTradeNo());
        paymentCallback.onPaymentSuccess(notify.getOutTradeNo(), null, java.time.LocalDateTime.now());
        return Result.success();
    }

    /**
     * 支付宝异步通知回调。
     *
     * <p>支付宝 POST 推送 application/x-www-form-urlencoded 数据，多次重试。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>从 HttpServletRequest 获取全部回调参数</li>
     *   <li>RSA2 验签（AlipaySignature.rsaCheckV1），防止伪造请求</li>
     *   <li>校验 trade_status = TRADE_SUCCESS，执行业务更新</li>
     *   <li>返回 "success"，支付宝停止推送</li>
     * </ol>
     */
    @Operation(summary = "支付宝异步通知回调")
    @PostMapping("/alipay")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>(16);
        Map<String, String[]> map = request.getParameterMap();
        for (String key : map.keySet()) {
            params.put(key, request.getParameter(key));
        }
        log.info("支付宝异步通知, trade_no={}, out_trade_no={}, trade_status={}",
                params.get("trade_no"), params.get("out_trade_no"), params.get("trade_status"));

        try {
            boolean signOk = AlipaySignature.rsaCheckV1(params,
                    alipayConfig.getAlipayPublicKey(), "UTF-8", "RSA2");
            if (!signOk) {
                log.warn("支付宝通知签名验证失败");
                return "fail";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝验签异常", e);
            return "fail";
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        if (TRADE_SUCCESS.equals(tradeStatus) || TRADE_FINISHED.equals(tradeStatus)) {
            paymentCallback.onPaymentSuccess(outTradeNo, null, java.time.LocalDateTime.now());
        } else {
            log.info("支付宝非终态通知 trade_status={}, orderNo={}", tradeStatus, outTradeNo);
        }

        return "success";
    }

    @Operation(summary = "微信支付异步通知回调")
    @PostMapping("/wechat")
    public void wechatNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 1. 读取原始请求体
        StringBuilder sb = new StringBuilder();
        String line;
        try (java.io.BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        log.info("微信支付回调: {}", body);

        // 2. 解析加密资源
        cn.hutool.json.JSONObject resultObj = cn.hutool.json.JSONUtil.parseObj(body);
        cn.hutool.json.JSONObject resource = resultObj.getJSONObject("resource");
        String ciphertext = resource.getStr("ciphertext");
        String nonce = resource.getStr("nonce");
        String associatedData = resource.getStr("associated_data");
        String apiV3Key = wechatPayConfig.getApiV3Key();

        String outTradeNo;
        if (apiV3Key != null && !apiV3Key.isBlank()) {
            // 生产环境：解密微信通知（AEAD_AES_256_GCM）
            String plaintext = decryptWechatNotify(associatedData, nonce, ciphertext, apiV3Key);
            log.info("微信回调解密后: {}", plaintext);
            cn.hutool.json.JSONObject data = cn.hutool.json.JSONUtil.parseObj(plaintext);
            outTradeNo = data.getStr("out_trade_no");
            String transactionId = data.getStr("transaction_id");
            log.info("微信支付成功 orderNo={}, transactionId={}", outTradeNo, transactionId);
        } else {
            // 开发环境（apiV3Key 未配置）：直接从请求体取 outTradeNo
            outTradeNo = resultObj.getStr("out_trade_no");
            if (outTradeNo == null) {
                outTradeNo = resultObj.getJSONObject("resource") != null
                        ? resultObj.getJSONObject("resource").getStr("out_trade_no")
                        : null;
            }
            log.info("微信回调（开发模式）, orderNo={}", outTradeNo);
        }

        if (outTradeNo != null) {
            paymentCallback.onPaymentSuccess(outTradeNo, null, java.time.LocalDateTime.now());
        }

        // 3. 返回成功响应给微信
        response.setStatus(200);
        response.setHeader("Content-type", "application/json");
        java.util.Map<String, String> resultMap = new java.util.HashMap<>();
        resultMap.put("code", "SUCCESS");
        resultMap.put("message", "SUCCESS");
        response.getOutputStream().write(cn.hutool.json.JSONUtil.toJsonStr(resultMap).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String decryptWechatNotify(String associatedData, String nonce, String ciphertext, String apiV3Key) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        javax.crypto.spec.SecretKeySpec key = new javax.crypto.spec.SecretKeySpec(
                apiV3Key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "AES");
        javax.crypto.spec.GCMParameterSpec spec = new javax.crypto.spec.GCMParameterSpec(128,
                nonce.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, spec);
        cipher.updateAAD(associatedData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] decrypted = cipher.doFinal(java.util.Base64.getDecoder().decode(ciphertext));
        return new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
    }
}
