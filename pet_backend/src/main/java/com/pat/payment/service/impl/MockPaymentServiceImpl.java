package com.pat.payment.service.impl;

import com.pat.payment.domain.PaymentStatus;
import com.pat.payment.domain.dto.PaymentContext;
import com.pat.payment.domain.dto.PaymentNotify;
import com.pat.payment.domain.vo.OrderPaymentVO;
import com.pat.payment.service.PaymentCallback;
import com.pat.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service("mockPaymentService")
public class MockPaymentServiceImpl implements PaymentService {

    @Override
    public OrderPaymentVO pay(PaymentContext context) {
        log.info("模拟支付下单 orderNo={}", context.getOrderNo());
        return new OrderPaymentVO(null, context.getOrderNo(),
                PaymentStatus.PENDING_PAY, context.getTotalAmount(), null, null, null);
    }

    @Override
    public void handleNotify(PaymentNotify notify, PaymentCallback callback) {
        log.info("模拟支付回调 orderNo={}", notify.getOutTradeNo());
        callback.onPaymentSuccess(notify.getOutTradeNo(), null, LocalDateTime.now());
    }
}
