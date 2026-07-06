package com.pat.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderCancelDTO;
import com.pat.order.domain.dto.OrderRefundDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderAdminMapper;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.IOrderAdminService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.order.service.support.OrderStateService;
import com.pat.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端订单操作 —— 编排层。
 *
 * <p>提供管理员视角的订单管理能力：取消订单、退款审核、直接退款、支付回调等。
 * 与 {@link OrderUserServiceImpl} 共享底层领域 Service。</p>
 */
@Slf4j
@Service
public class OrderAdminServiceImpl implements IOrderAdminService {

    private final PurchaseOrderBaseService baseService;
    private final OrderAdminMapper orderAdminMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;
    private final OrderStateService orderStateService;

    public OrderAdminServiceImpl(PurchaseOrderBaseService baseService,
                                 OrderAdminMapper orderAdminMapper,
                                 OrderItemMapper orderItemMapper,
                                 ProductService productService,
                                 OrderStateService orderStateService) {
        this.baseService = baseService;
        this.orderAdminMapper = orderAdminMapper;
        this.orderItemMapper = orderItemMapper;
        this.productService = productService;
        this.orderStateService = orderStateService;
    }

    /**
     * 管理员取消订单。
     *
     * <p>恢复库存后通过 {@link OrderStateService} 执行状态变更，取消类型标记为 admin。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrderCancelDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.CANCELLED.getCode());

        restoreOrderStock(order.getId());
        orderStateService.cancel(order, dto.getCancelReason(), "admin");
        log.info("管理员取消订单 orderId={}, reason={}", dto.getOrderId(), dto.getCancelReason());
    }

    /**
     * 退款审核。
     *
     * <p>审核通过时：恢复库存并置为已退款。<br>
     * 审核驳回时：恢复到 preRefundStatus 记录的状态（优先），
     * 若无记录则根据 receiveTime 推断（已收货→SHIPPED，未收货→RECEIVED）。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundApprove(OrderRefundDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());

        if (dto.getApproved()) {
            OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.REFUNDED.getCode());
            restoreOrderStock(order.getId());
            orderStateService.approveRefund(order);
        } else {
            Integer restoreStatus = order.getPreRefundStatus();
            if (restoreStatus == null) {
                restoreStatus = (order.getReceiveTime() != null)
                        ? OrderStatus.RECEIVED.getCode()
                        : OrderStatus.SHIPPED.getCode();
            }
            OrderStateMachine.validate(order.getOrderStatus(), restoreStatus);
            orderStateService.rejectRefund(order, restoreStatus, dto.getRejectReason());
        }
        log.info("退款审核 orderId={}, approved={}", dto.getOrderId(), dto.getApproved());
    }

    /**
     * 直接退款（管理员主动操作）。
     *
     * <p>不经过用户申请流程，直接由管理员操作退款。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundDirect(OrderCancelDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.REJECTED.getCode());

        restoreOrderStock(order.getId());
        orderStateService.directRefund(order, dto.getCancelReason());
        log.info("直接退款 orderId={}, reason={}", dto.getOrderId(), dto.getCancelReason());
    }

    /**
     * 支付成功回调（管理端）。
     *
     * <p>更新订单状态为已支付，并批量标记相关商品为已售出。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            log.warn("paySuccess 订单不存在 {}", orderNo);
            return;
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        orderStateService.paySuccess(order, LocalDateTime.now());

        int count = orderAdminMapper.batchMarkProductsAsSold(order.getId());
        log.info("支付成功 orderNo={}, 已更新{}件商品为已售出", orderNo, count);
    }

    /** 按 ID 查询订单，不存在则抛异常。 */
    private PurchaseOrder getOrderById(Long id) {
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        return order;
    }

    /** 恢复订单相关商品的库存。 */
    private void restoreOrderStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem item : items) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }
    }
}