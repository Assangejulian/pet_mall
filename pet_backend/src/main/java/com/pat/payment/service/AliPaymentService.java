package com.pat.payment.service;

import com.alipay.api.AlipayApiException;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.helper.OrderStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("ALIPAYPaymentService")
public class AliPaymentService implements PaymentService {

    private final AlipayPayService alipayPayService;

    public AliPaymentService(AlipayPayService alipayPayService) {
        this.alipayPayService = alipayPayService;
    }

    @Override
    public OrderPaymentVO pay(PurchaseOrder order) {
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());

        String total = order.getPayAmount().setScale(2, java.math.RoundingMode.HALF_UP).toString();
        String subject = "宠铺 - " + order.getOrderNo();

        try {
            String form = alipayPayService.createWapPayPage(
                    order.getOrderNo(), subject, "", total);

            log.info("支付宝手机网站支付下单 orderNo={}, total={}", order.getOrderNo(), total);
            return new OrderPaymentVO(order.getId(), order.getOrderNo(),
                    Integer.valueOf(OrderStatus.PENDING_PAY.getCode()), order.getPayAmount(), form, null);
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
