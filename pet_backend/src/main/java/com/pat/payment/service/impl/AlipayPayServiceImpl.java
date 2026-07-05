package com.pat.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
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
     * 创建支付宝扫码支付（当面付），返回 qr_code 链接
     */
    public String createQrPayUrl(String outTradeNo,
                                 String subject,
                                 String body,
                                 BigDecimal totalAmount) throws AlipayApiException {
        if (isBlank(alipayConfig.getAppId()) || isBlank(alipayConfig.getPrivateKey())) {
            log.warn("Alipay config is incomplete, returning mock QR URL orderNo={}", outTradeNo);
            return "alipay-mock://qr?orderNo=" + outTradeNo;
        }

        DefaultAlipayClient client = buildClient();

        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(outTradeNo);
        model.setSubject(subject);
        model.setBody(body);
        model.setTotalAmount(totalAmount.setScale(2, java.math.RoundingMode.HALF_UP).toString());

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        AlipayTradePrecreateResponse response = client.execute(request);
        log.info("支付宝扫码下单响应 orderNo={}, code={}, msg={}, subCode={}, subMsg={}",
                outTradeNo, response.getCode(), response.getMsg(),
                response.getSubCode(), response.getSubMsg());
        if (!response.isSuccess()) {
            String errMsg = response.getSubMsg() != null ? response.getSubMsg() : response.getMsg();
            log.error("支付宝扫码下单失败 orderNo={}, code={}, msg={}, subCode={}, subMsg={}, body={}",
                    outTradeNo, response.getCode(), response.getMsg(),
                    response.getSubCode(), response.getSubMsg(), response.getBody());
            throw new RuntimeException("支付宝支付下单失败: " + (errMsg != null ? errMsg : response.getCode()));
        }

        log.info("支付宝扫码下单成功 orderNo={}, qrCode={}", outTradeNo, response.getQrCode());
        return response.getQrCode();
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

    public void handleNotify(PaymentNotify notify, PaymentCallback callback) {
        log.info("支付宝支付回调 orderNo={}", notify.getOutTradeNo());
        callback.onPaymentSuccess(notify.getOutTradeNo(), null, LocalDateTime.now());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}