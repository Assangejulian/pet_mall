package com.pat.order.service.payment;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.service.base.PurchaseOrderBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 模拟支付 —— 默认支付方式，直接标记订单为已支付
 */
@Slf4j
@Service("mockPaymentService")
public class MockPaymentService implements PaymentService {

    private final PurchaseOrderBaseService baseService;

    public MockPaymentService(PurchaseOrderBaseService baseService) {
        this.baseService = baseService;
    }

    @Override
    public OrderPaymentVO pay(PurchaseOrder order) {
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("模拟支付成功 orderNo={}", order.getOrderNo());
        return new OrderPaymentVO(order.getId(), order.getOrderNo(),
                OrderStatus.PAID.getCode(), order.getPayAmount());
    }

    @Override
    public void handleNotify(PayNotifyDTO dto) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, dto.getOutTradeNo())
                .one();
        if (order == null) {
            log.warn("支付回调：订单不存在 {}", dto.getOutTradeNo());
            return;
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("模拟支付回调成功 orderNo={}", dto.getOutTradeNo());
    }
}
