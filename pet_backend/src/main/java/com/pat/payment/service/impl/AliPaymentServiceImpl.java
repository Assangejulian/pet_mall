package com.pat.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.helper.OrderStateMachine;
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
    public OrderPaymentVO pay(PurchaseOrder order) {
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());

        String total = order.getPayAmount().setScale(2, java.math.RoundingMode.HALF_UP).toString();
        String subject = "宠铺 - " + order.getOrderNo();

        try {
            // form: PC/H5 使用的自动提交表单
            String form = alipayPayService.createWapPayPage(
                    order.getOrderNo(), subject, "", total);
            // payUrl: 小程序 webview 可用的跳转链接
            String payUrl = alipayPayService.createWapPayUrl(
                    order.getOrderNo(), subject, "", total);

            log.info("支付宝手机网站支付下单 orderNo={}, total={}", order.getOrderNo(), total);
            return new OrderPaymentVO(order.getId(), order.getOrderNo(),
                    OrderStatus.PENDING_PAY.getCode(), order.getPayAmount(), form, null, payUrl);
        } catch (AlipayApiException e) {
            log.error("支付宝下单异常 orderNo={}", order.getOrderNo(), e);
            throw new RuntimeException("支付宝支付下单失败: " + e.getErrMsg());
        }
    }

    @Override
    public void handleNotify(PayNotifyDTO dto) {
        alipayPayService.handleNotify(dto);
    }
}
