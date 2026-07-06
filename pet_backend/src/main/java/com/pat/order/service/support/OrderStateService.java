package com.pat.order.service.support;

import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.service.base.PurchaseOrderBaseService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 订单状态变更执行器。
 *
 * <p>负责执行状态变更的具体动作（set 字段 + 持久化），不负责校验状态转移合法性。
 * 合法性校验由 {@link com.pat.order.helper.OrderStateMachine} 负责。</p>
 */
@Service
public class OrderStateService {

    private final PurchaseOrderBaseService baseService;

    public OrderStateService(PurchaseOrderBaseService baseService) {
        this.baseService = baseService;
    }

    /**
     * 取消订单。
     *
     * @param order      订单
     * @param reason     取消原因
     * @param cancelType 取消方：user（用户）、admin（管理员）、system（系统超时）
     */
    public void cancel(PurchaseOrder order, String reason, String cancelType) {
        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(reason);
        order.setCancelType(cancelType);
        order.setCancelTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 确认收货。 */
    public void receive(PurchaseOrder order) {
        order.setOrderStatus(OrderStatus.RECEIVED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /**
     * 评价完成。
     *
     * @param order 订单
     * @param now   评价时间
     */
    public void evaluate(PurchaseOrder order, LocalDateTime now) {
        order.setOrderStatus(OrderStatus.EVALUATED.getCode());
        order.setEvaluateTime(now);
        baseService.updateById(order);
    }

    /**
     * 申请退款。
     *
     * <p>保存当前状态到 preRefundStatus，用于管理员驳回时恢复到正确状态。</p>
     *
     * @param order  订单
     * @param reason 退款原因
     */
    public void applyRefund(PurchaseOrder order, String reason) {
        order.setPreRefundStatus(order.getOrderStatus());
        order.setOrderStatus(OrderStatus.REFUNDING.getCode());
        order.setCancelReason(reason);
        order.setRefundApplyTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 退款审核通过。 */
    public void approveRefund(PurchaseOrder order) {
        order.setOrderStatus(OrderStatus.REFUNDED.getCode());
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /**
     * 退款审核驳回。
     *
     * @param order          订单
     * @param restoreStatus  恢复到的状态（preRefundStatus）
     * @param rejectReason   驳回原因
     */
    public void rejectRefund(PurchaseOrder order, Integer restoreStatus, String rejectReason) {
        order.setOrderStatus(restoreStatus);
        order.setCancelReason(rejectReason);
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /** 直接退款（管理员主动操作，不经过退款申请流程）。 */
    public void directRefund(PurchaseOrder order, String reason) {
        order.setOrderStatus(OrderStatus.REJECTED.getCode());
        order.setCancelReason(reason);
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    /**
     * 支付成功。
     *
     * @param order   订单
     * @param payTime 支付时间
     */
    public void paySuccess(PurchaseOrder order, LocalDateTime payTime) {
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(payTime);
        baseService.updateById(order);
    }
}