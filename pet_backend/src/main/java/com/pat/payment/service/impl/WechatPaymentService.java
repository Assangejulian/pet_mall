package com.pat.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.payment.config.WechatPayConfig;
import com.pat.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;
import java.security.Signature;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service("WECHATPaymentService")
public class WechatPaymentService implements PaymentService {

    private final WechatPayConfig wechatPayConfig;
    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;

    public WechatPaymentService(WechatPayConfig wechatPayConfig,
                                PurchaseOrderBaseService baseService,
                                OrderItemMapper orderItemMapper,
) {
        this.wechatPayConfig = wechatPayConfig;
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
    }

    /**
     * 微信小程序 JSAPI 支付。<br>
     * 返回参数供小程序端调用 wx.requestPayment()。
     */
    @Override
    public OrderPaymentVO pay(PurchaseOrder order) {
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());

        String prepayId = createWechatTransaction(order);

        // 签名 JSAPI 调起支付参数
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = IdUtil.fastSimpleUUID();
        String packageStr = "prepay_id=" + prepayId;
        String paySign = signJsapi(wechatPayConfig.getAppid(), timeStamp, nonceStr, packageStr);

        Map<String, String> payParams = new LinkedHashMap<>();
        payParams.put("appId", wechatPayConfig.getAppid());
        payParams.put("timeStamp", timeStamp);
        payParams.put("nonceStr", nonceStr);
        payParams.put("package", packageStr);
        payParams.put("signType", "RSA");
        payParams.put("paySign", paySign);

        log.info("微信小程序支付下单成功 orderNo={}, prepayId={}", order.getOrderNo(), prepayId);
        return new OrderPaymentVO(order.getId(), order.getOrderNo(),
                Integer.valueOf(OrderStatus.PENDING_PAY.getCode()), order.getPayAmount(), null,
                JSONUtil.toJsonStr(payParams));
    }

    /**
     * 处理微信支付结果回调。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(PayNotifyDTO dto) {
        log.info("微信支付回调 orderNo={}", dto.getOutTradeNo());
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, dto.getOutTradeNo())
                .one();
        if (order == null) {
            log.warn("微信回调：订单不存在 {}", dto.getOutTradeNo());
            return;
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatus.PAID.getCode()) {
            log.info("微信回调：订单已支付，跳过重复处理 {}", dto.getOutTradeNo());
            return;
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());

        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("微信支付成功 orderNo={}", dto.getOutTradeNo());
    }

    // ==================== 微信支付 API v3 ====================

    /**
     * 调用微信支付 API v3 统一下单（JSAPI）。
     * 使用商户私钥签名 JWT token 进行 API 认证。
     */
    private String createWechatTransaction(PurchaseOrder order) {
        String mchId = wechatPayConfig.getMchId();
        String appid = wechatPayConfig.getAppid();
        String privateKeyPem = wechatPayConfig.getPrivateKey();
        String notifyUrl = wechatPayConfig.getNotifyUrl();

        if (mchId == null || mchId.isBlank() || privateKeyPem == null || privateKeyPem.isBlank()) {
            log.warn("微信支付未配置完整，使用 mock prepay_id orderNo={}", order.getOrderNo());
            return "mock_prepay_id_" + order.getOrderNo();
        }

        try {
            String description = buildOrderDesc(order);
            String amount = order.getPayAmount().setScale(2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).toBigInteger().toString();

            JSONObject body = new JSONObject();
            body.set("appid", appid);
            body.set("mchid", mchId);
            body.set("description", description);
            body.set("out_trade_no", order.getOrderNo());
            body.set("notify_url", notifyUrl);

            JSONObject amountObj = new JSONObject();
            amountObj.set("total", Integer.parseInt(amount));
            amountObj.set("currency", "CNY");
            body.set("amount", amountObj);

            JSONObject payer = new JSONObject();
            payer.set("openid", "");
            body.set("payer", payer);

            String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
            String method = "POST";
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonce = IdUtil.fastSimpleUUID();
            String bodyStr = JSONUtil.toJsonStr(body);

            // Build signature string
            String signStr = method + "\n" + url + "\n" + timestamp + "\n" + nonce + "\n" + bodyStr + "\n";
            String token = "mchid=\"" + mchId + "\","
                    + "nonce_str=\"" + nonce + "\","
                    + "timestamp=\"" + timestamp + "\","
                    + "serial_no=\"" + wechatPayConfig.getMchSerialNo() + "\","
                    + "signature=\"" + rsaSign(signStr, privateKeyPem) + "\"";

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi").openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);
            conn.setRequestProperty("User-Agent", "pet-store");
            conn.setDoOutput(true);
            conn.getOutputStream().write(bodyStr.getBytes(StandardCharsets.UTF_8));

            int statusCode = conn.getResponseCode();
            String respBody = new String(
                    (statusCode == 200 ? conn.getInputStream() : conn.getErrorStream())
                            .readAllBytes(), StandardCharsets.UTF_8);
            log.info("微信统一下单响应 status={}, body={}", statusCode, respBody);

            if (statusCode == 200 || statusCode == 201) {
                JSONObject resp = JSONUtil.parseObj(respBody);
                return resp.getStr("prepay_id");
            } else {
                log.error("微信统一下单失败 status={}, resp={}", statusCode, respBody);
                throw new BusinessException(500, "微信支付下单失败", null);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信统一下单异常 orderNo={}", order.getOrderNo(), e);
            throw new BusinessException(500, "微信支付下单异常", null);
        }
    }

    /**
     * 签名 JSAPI 调起支付参数（RSA 模式）。
     */
    private String signJsapi(String appId, String timeStamp, String nonceStr, String packageStr) {
        String signStr = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageStr + "\n";
        return rsaSign(signStr, wechatPayConfig.getPrivateKey());
    }

    private String rsaSign(String content, String privateKeyPem) {
        try {
            String cleaned = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(cleaned);
            java.security.spec.PKCS8EncodedKeySpec spec =
                    new java.security.spec.PKCS8EncodedKeySpec(encoded);
            java.security.KeyFactory factory = java.security.KeyFactory.getInstance("RSA");
            java.security.PrivateKey key = factory.generatePrivate(spec);

            Signature sig = Signature.getInstance("SHA256WithRSA");
            sig.initSign(key);
            sig.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            log.error("RSA sign failed", e);
            throw new BusinessException(500, "签名失败", null);
        }
    }

    private String buildOrderDesc(PurchaseOrder order) {
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size() && i < 3; i++) {
            OrderItem item = items.get(i);
            if (sb.length() > 0) sb.append("; ");
            sb.append(item.getProductName()).append(" x").append(item.getQuantity());
        }
        if (items.size() > 3) sb.append("...");
        return sb.length() > 0 ? sb.toString() : "宠铺 - " + order.getOrderNo();
    }
}
