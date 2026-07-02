package com.pat.payment.service;

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

@Slf4j
@Service("mockPaymentService")
public class MockPaymentService implements PaymentService {

    private final PurchaseOrderBaseService baseService;

    public MockPaymentService(PurchaseOrderBaseService baseService) {
        this.baseService = baseService;
    }

    @Override
    /**
     * 模拟支付（开发/测试用）。校验状态机后置为已支付。
     *
     * @param order 待支付订单
     * @return 支付结果 VO
     */
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
    /**
     * 模拟支付回调处理。
     *
     * @param dto 回调参数
     */
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