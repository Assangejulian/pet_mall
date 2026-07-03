package com.pat.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.pat.common.domain.Result;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.payment.config.AlipayConfig;
import com.pat.payment.service.impl.AlipayPayServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方支付回调通知控制器。
 *
 * <p>接收支付宝/微信的异步通知，验签后更新订单状态。</p>
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
    private final AlipayPayServiceImpl alipayPayService;

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String TRADE_FINISHED = "TRADE_FINISHED";

    public PayNotifyController(AlipayConfig alipayConfig,
                               AlipayPayServiceImpl alipayPayService) {
        this.alipayConfig = alipayConfig;
        this.alipayPayService = alipayPayService;
    }

    @Operation(summary = "模拟支付成功回调（开发测试用）")
    @PostMapping("/pay-success")
    public Result<Void> paySuccess(@RequestBody PayNotifyDTO dto) {
        log.info("模拟支付回调, 订单号: {}", dto.getOutTradeNo());
        alipayPayService.handleNotify(dto);
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
        // 1. 获取全部回调参数
        Map<String, String> params = new HashMap<>(16);
        Map<String, String[]> map = request.getParameterMap();
        for (String key : map.keySet()) {
            params.put(key, request.getParameter(key));
        }
        log.info("支付宝异步通知, trade_no={}, out_trade_no={}, trade_status={}",
                params.get("trade_no"), params.get("out_trade_no"), params.get("trade_status"));

        // 2. RSA2 验签，防止伪造请求
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

        // 3. 支付成功执行业务：修改订单状态、扣库存
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        if (TRADE_SUCCESS.equals(tradeStatus) || TRADE_FINISHED.equals(tradeStatus)) {
            PayNotifyDTO dto = new PayNotifyDTO();
            dto.setOutTradeNo(outTradeNo);
            alipayPayService.handleNotify(dto);
        } else {
            log.info("支付宝非终态通知 trade_status={}, orderNo={}", tradeStatus, outTradeNo);
        }

        // 返回 success，支付宝停止重复推送通知
        return "success";
    }

    @Operation(summary = "微信支付异步通知回调")
    @PostMapping("/wechat")
    public Result<Void> wechatNotify(@RequestBody PayNotifyDTO dto) {
        log.info("微信回调, 订单号: {}", dto.getOutTradeNo());
        return Result.success();
    }
}
