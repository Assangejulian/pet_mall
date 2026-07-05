package com.pat.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.pat.payment.config.AlipayConfig;
import com.pat.payment.domain.dto.PaymentNotify;
import com.pat.payment.service.PaymentCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AlipayPayServiceImpl {

    private final AlipayConfig alipayConfig;

    public AlipayPayServiceImpl(AlipayConfig alipayConfig) {
        this.alipayConfig = alipayConfig;
    }

    /**
     * 创建支付宝手机网站支付表单（POST 方式，返回 HTML 表单）
     */
    public String createWapPayPage(String outTradeNo,
                                   String subject,
                                   String body,
                                   String totalAmount) throws AlipayApiException {
        if (isBlank(alipayConfig.getAppId()) || isBlank(alipayConfig.getPrivateKey())) {
            log.warn("Alipay config is incomplete, returning mock pay form orderNo={}", outTradeNo);
            return "<form data-mock=\"alipay\" data-order-no=\"" + outTradeNo + "\"></form>";
        }

        DefaultAlipayClient client = buildClient();
        AlipayTradeWapPayRequest request = buildWapPayRequest(outTradeNo, subject, body, totalAmount);
        return client.pageExecute(request).getBody();
    }

    /**
     * 创建支付宝手机网站支付跳转 URL（GET 方式，适合小程序 webview 打开）
     */
    public String createWapPayUrl(String outTradeNo,
                                  String subject,
                                  String body,
                                  String totalAmount) throws AlipayApiException {
        if (isBlank(alipayConfig.getAppId()) || isBlank(alipayConfig.getPrivateKey())) {
            log.warn("Alipay config is incomplete, returning mock URL orderNo={}", outTradeNo);
            return "#";
        }

        DefaultAlipayClient client = buildClient();
        AlipayTradeWapPayRequest request = buildWapPayRequest(outTradeNo, subject, body, totalAmount);
        return client.pageExecute(request, "GET").getBody();
    }

    private DefaultAlipayClient buildClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGateway(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType());
    }

    private AlipayTradeWapPayRequest buildWapPayRequest(String outTradeNo,
                                                         String subject,
                                                         String body,
                                                         String totalAmount) {
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(outTradeNo);
        model.setSubject(subject);
        model.setBody(body);
        model.setTotalAmount(totalAmount);
        model.setProductCode("QUICK_WAP_WAY");

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());
        return request;
    }

    public void handleNotify(PaymentNotify notify, PaymentCallback callback) {
        log.info("支付宝支付回调 orderNo={}", notify.getOutTradeNo());
        callback.onPaymentSuccess(notify.getOutTradeNo(), null, LocalDateTime.now());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
