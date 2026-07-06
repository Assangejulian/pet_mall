package com.pat.order.service.support;

import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.service.base.PurchaseOrderBaseService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 订单状态变更执行器。
 * <p>只负责"转过去"的动作（set 字段 + 持久化），不负责"能不能转"（那是 OrderStateMachine 的事）。</p>
 */
@Service
public class OrderStateService {

    private final PurchaseOrderBaseService baseService;

    public OrderStateService(PurchaseOrderBaseService baseService) {
        this.baseService = baseService;
    }

    /** 取消订单（cancelType: user / admin / system） */
    public void cancel(PurchaseOrder order, String reason, String cancelType) {
        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(reason);
        order.setCancelType(cancelType);
        order.setCancelTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 确认收货 */
    public void receive(PurchaseOrder order) {
        order.setOrderStatus(OrderStatus.RECEIVED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 评价完成 */
    public void evaluate(PurchaseOrder order, LocalDateTime now) {
        order.setOrderStatus(OrderStatus.EVALUATED.getCode());
        order.setEvaluateTime(now);
        baseService.updateById(order);
    }

    /** 申请退款（保存退款前状态） */
    public void applyRefund(PurchaseOrder order, String reason) {
        order.setPreRefundStatus(order.getOrderStatus());
        order.setOrderStatus(OrderStatus.REFUNDING.getCode());
        order.setCancelReason(reason);
        order.setRefundApplyTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 退款审核通过 */
    public void approveRefund(PurchaseOrder order) {
        order.setOrderStatus(OrderStatus.REFUNDED.getCode());
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 退款审核驳回（恢复到退款前的状态） */
    public void rejectRefund(PurchaseOrder order, Integer restoreStatus, String rejectReason) {
        order.setOrderStatus(restoreStatus);
        order.setCancelReason(rejectReason);
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 直接退款（管理员主动操作） */
    public void directRefund(PurchaseOrder order, String reason) {
        order.setOrderStatus(OrderStatus.REJECTED.getCode());
        order.setCancelReason(reason);
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 支付成功 */
    public void paySuccess(PurchaseOrder order, LocalDateTime payTime) {
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(payTime);
        baseService.updateById(order);
    }
}