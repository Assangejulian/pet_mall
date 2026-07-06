package com.pat.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.pat.payment.domain.PaymentStatus;
import com.pat.payment.domain.dto.PaymentContext;
import com.pat.payment.domain.dto.PaymentNotify;
import com.pat.payment.domain.vo.OrderPaymentVO;
import com.pat.payment.service.PaymentCallback;
import com.pat.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("ALIPAYPaymentService")
public class AliPaymentServiceImpl implements PaymentService {

    private final AlipayPayServiceImpl alipayPayService;

    public AliPaymentServiceImpl(AlipayPayServiceImpl alipayPayService) {
        this.alipayPayService = alipayPayService;
    }

    @Override
    public OrderPaymentVO pay(PaymentContext context) {
        String subject = "宠物商城 - " + context.getOrderNo();

        try {
            String qrCode = alipayPayService.createQrPayUrl(
                    context.getOrderNo(), subject, "", context.getTotalAmount());

            log.info("支付宝扫码支付下单 orderNo={}, amount={}", context.getOrderNo(), context.getTotalAmount());
                        return new OrderPaymentVO(null, context.getOrderNo(),
                    PaymentStatus.PENDING_PAY, context.getTotalAmount(), null, null, qrCode);
        } catch (AlipayApiException e) {
            log.error("支付宝扫码下单异常 orderNo={}", context.getOrderNo(), e);
            throw new RuntimeException("支付宝支付下单失败: " + e.getErrMsg());
        }
    }

    @Override
    public void handleNotify(PaymentNotify notify, PaymentCallback callback) {
        alipayPayService.handleNotify(notify, callback);
    }
}