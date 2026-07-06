package com.pat.order.service.impl;

import lombok.extern.slf4j.Slf4j;
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
import com.pat.product.service.ProductService;
import com.pat.order.service.IOrderAdminService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderAdminServiceImpl implements IOrderAdminService {

    private final PurchaseOrderBaseService baseService;
    private final OrderAdminMapper orderAdminMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;

    public OrderAdminServiceImpl(PurchaseOrderBaseService baseService,
                                 OrderAdminMapper orderAdminMapper,
                                 OrderItemMapper orderItemMapper,
                                 ProductService productService) {
        this.baseService = baseService;
        this.orderAdminMapper = orderAdminMapper;
        this.orderItemMapper = orderItemMapper;
        this.productService = productService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 取消订单（管理端）。校验状态机后执行取消，记录取消原因和时间。
     *
     * @param dto 取消参数（订单 ID + 原因）
     */
    public void cancelOrder(OrderCancelDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.CANCELLED.getCode());

        restoreOrderStock(order.getId());
        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelType("admin");
        order.setCancelReason(dto.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("订单取消 orderId={}, reason={}", dto.getOrderId(), dto.getCancelReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 退款审核（通过/驳回）。通过则置为已退款，驳回则恢复到退款前的状态。
     *
     * @param dto 退款审核参数
     */
    public void refundApprove(OrderRefundDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        Integer current = order.getOrderStatus();

        if (dto.getApproved()) {
            OrderStateMachine.validate(current, OrderStatus.REFUNDED.getCode());
            restoreOrderStock(order.getId());
            order.setOrderStatus(OrderStatus.REFUNDED.getCode());
        } else {
            // 驳回时恢复到退款前的状态（preRefundStatus），或根据时间戳推断
            Integer restoreStatus = order.getPreRefundStatus();
            if (restoreStatus == null) {
                restoreStatus = (order.getReceiveTime() != null)
                        ? OrderStatus.RECEIVED.getCode()
                        : OrderStatus.SHIPPED.getCode();
            }
            OrderStateMachine.validate(current, restoreStatus);
            order.setOrderStatus(restoreStatus);
            order.setCancelReason(dto.getRejectReason());
        }
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("退款审核 orderId={}, approved={}, reason={}", dto.getOrderId(), dto.getApproved(), dto.getRejectReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 直接退款（不经过申请流程）。用于管理端主动退款操作。
     *
     * @param dto 退款参数
     */
    public void refundDirect(OrderCancelDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.REJECTED.getCode());

        restoreOrderStock(order.getId());
        order.setOrderStatus(OrderStatus.REJECTED.getCode());
        order.setCancelReason(dto.getCancelReason());
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("直接退款 orderId={}, reason={}", dto.getOrderId(), dto.getCancelReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 支付成功回调。将订单置为已支付状态，并批量标记对应商品为已售出。
     *
     * @param orderNo 订单号
     */
    public void paySuccess(String orderNo) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            log.warn("paySuccess 订单不存在: {}", orderNo);
            return;
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);

        int count = orderAdminMapper.batchMarkProductsAsSold(order.getId());
        log.info("支付成功 orderNo={}, 已更新{}件商品为已售出", orderNo, count);
    }

    private PurchaseOrder getOrderById(Long id) {
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        return order;
    }

    private void restoreOrderStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem item : items) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
            log.info("恢复库存 productId={}, quantity={}, orderId={}",
                    item.getProductId(), item.getQuantity(), orderId);
        }
    }
}