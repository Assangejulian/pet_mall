package com.pat.payment.service.impl;

import com.pat.payment.domain.PaymentStatus;
import com.pat.payment.domain.dto.PaymentContext;
import com.pat.payment.domain.dto.PaymentNotify;
import com.pat.payment.domain.vo.OrderPaymentVO;
import com.pat.payment.service.PaymentCallback;
import com.pat.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service("mockPaymentService")
public class MockPaymentServiceImpl implements PaymentService {

    private final PaymentCallback paymentCallback;

    public MockPaymentServiceImpl(@Lazy PaymentCallback paymentCallback) {
        this.paymentCallback = paymentCallback;
    }

    @Override
    public OrderPaymentVO pay(PaymentContext context) {
        log.info("模拟支付下单 orderNo={}", context.getOrderNo());
        // 直接同步回调，更新订单状态为已支付
        paymentCallback.onPaymentSuccess(context.getOrderNo(), context.getTotalAmount(), LocalDateTime.now());
        return new OrderPaymentVO(null, context.getOrderNo(),
                PaymentStatus.PAID, context.getTotalAmount(), null, null, null);
    }

    @Override
    public void handleNotify(PaymentNotify notify, PaymentCallback callback) {
        log.info("模拟支付回调 orderNo={}", notify.getOutTradeNo());
        callback.onPaymentSuccess(notify.getOutTradeNo(), null, LocalDateTime.now());
    }
}
