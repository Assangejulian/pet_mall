package com.pat.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.pat.payment.config.WechatPayConfig;
import com.pat.payment.domain.PaymentStatus;
import com.pat.payment.domain.dto.PaymentContext;
import com.pat.payment.domain.dto.PaymentNotify;
import com.pat.payment.domain.vo.OrderPaymentVO;
import com.pat.payment.service.PaymentCallback;
import com.pat.payment.service.PaymentService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.cipher.Signer;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service("WECHATPaymentService")
public class WechatPaymentServiceImpl implements PaymentService {

    private final WechatPayConfig payConfig;

    public WechatPaymentServiceImpl(WechatPayConfig payConfig) {
        this.payConfig = payConfig;
    }

    @Override
    public OrderPaymentVO pay(PaymentContext context) {
        if (isBlank(payConfig.getMchId()) || isBlank(payConfig.getPrivateKey())) {
            log.warn("微信支付未配置完整，使用 mock prepay_id orderNo={}", context.getOrderNo());
            return mockResult(context);
        }

        try {
            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(payConfig.getMchId())
                    .privateKey(payConfig.getPrivateKey())
                    .merchantSerialNumber(payConfig.getMchSerialNo())
                    .apiV3Key(payConfig.getApiV3Key())
                    .build();

            JsapiService service = new JsapiService.Builder().config(config).build();

            PrepayRequest request = new PrepayRequest();
            request.setAppid(payConfig.getAppid());
            request.setMchid(payConfig.getMchId());
            request.setOutTradeNo(context.getOrderNo());
            request.setDescription("宠物商城 - " + context.getOrderNo());
            request.setNotifyUrl(payConfig.getNotifyUrl());

            Amount amount = new Amount();
            amount.setTotal(context.getTotalAmount().multiply(new BigDecimal("100")).intValue());
            amount.setCurrency("CNY");
            request.setAmount(amount);

            Payer payer = new Payer();
            payer.setOpenid(context.getOpenid() != null ? context.getOpenid() : "");
            request.setPayer(payer);

            PrepayResponse resp = service.prepay(request);
            String prepayId = resp.getPrepayId();

            long timestamp = System.currentTimeMillis() / 1000;
            String nonceStr = IdUtil.fastSimpleUUID();
            String packageStr = "prepay_id=" + prepayId;
            String signSrc = payConfig.getAppid() + "\n" + timestamp + "\n" + nonceStr + "\n" + packageStr + "\n";

            Signer signer = config.createSigner();
            String paySign = signer.sign(signSrc).getSign();

            Map<String, String> params = new LinkedHashMap<>();
            params.put("appId", payConfig.getAppid());
            params.put("timeStamp", String.valueOf(timestamp));
            params.put("nonceStr", nonceStr);
            params.put("package", packageStr);
            params.put("signType", "RSA");
            params.put("paySign", paySign);

            log.info("微信小程序支付下单成功 orderNo={}, prepayId={}", context.getOrderNo(), prepayId);
            return new OrderPaymentVO(null, context.getOrderNo(),
                    PaymentStatus.PENDING_PAY, context.getTotalAmount(), null,
                    JSONUtil.toJsonStr(params), null);

        } catch (Exception e) {
            log.error("微信支付下单异常 orderNo={}", context.getOrderNo(), e);
            throw new RuntimeException("微信支付下单失败: " + e.getMessage());
        }
    }

    @Override
    public void handleNotify(PaymentNotify notify, PaymentCallback callback) {
        log.info("微信支付回调 orderNo={}", notify.getOutTradeNo());
        callback.onPaymentSuccess(notify.getOutTradeNo(), null, java.time.LocalDateTime.now());
    }

    private OrderPaymentVO mockResult(PaymentContext context) {
        long ts = System.currentTimeMillis() / 1000;
        String nonce = IdUtil.fastSimpleUUID();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("appId", payConfig.getAppid());
        params.put("timeStamp", String.valueOf(ts));
        params.put("nonceStr", nonce);
        params.put("package", "prepay_id=mock_" + context.getOrderNo());
        params.put("signType", "RSA");
        params.put("paySign", "mock_sign");
        return new OrderPaymentVO(null, context.getOrderNo(),
                PaymentStatus.PENDING_PAY, context.getTotalAmount(), null,
                JSONUtil.toJsonStr(params), null);
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}
