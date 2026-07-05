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
        String total = context.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toString();
        String subject = "宠物商城 - " + context.getOrderNo();

        try {
            String form = alipayPayService.createWapPayPage(
                    context.getOrderNo(), subject, "", total);
            String payUrl = alipayPayService.createWapPayUrl(
                    context.getOrderNo(), subject, "", total);

            log.info("支付宝手机网站支付下单 orderNo={}, total={}", context.getOrderNo(), total);
            return new OrderPaymentVO(null, context.getOrderNo(),
                    PaymentStatus.PENDING_PAY, context.getTotalAmount(), form, null, payUrl);
        } catch (AlipayApiException e) {
            log.error("支付宝下单异常 orderNo={}", context.getOrderNo(), e);
            throw new RuntimeException("支付宝支付下单失败: " + e.getErrMsg());
        }
    }

    @Override
    public void handleNotify(PaymentNotify notify, PaymentCallback callback) {
        alipayPayService.handleNotify(notify, callback);
    }
}
