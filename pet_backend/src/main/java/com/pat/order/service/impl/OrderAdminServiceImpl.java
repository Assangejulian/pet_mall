package com.pat.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderCancelDTO;
import com.pat.order.domain.dto.OrderRefundDTO;
import com.pat.order.domain.dto.OrderShipDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.IOrderAdminService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理端订单操作：列表、发货、取消、退款审核
 */
@Service
public class OrderAdminServiceImpl implements IOrderAdminService {

    private static final Logger log = LoggerFactory.getLogger(OrderAdminServiceImpl.class);

    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;

    public OrderAdminServiceImpl(PurchaseOrderBaseService baseService,
                                 OrderItemMapper orderItemMapper) {
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public IPage<PurchaseOrder> pageList(PurchaseOrder param, Page<PurchaseOrder> page) {
        QueryWrapper<PurchaseOrder> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (param != null) {
            if (param.getOrderStatus() != null) wrapper.eq("order_status", param.getOrderStatus());
            if (param.getUserId() != null) wrapper.eq("user_id", param.getUserId());
            if (param.getOrderNo() != null && !param.getOrderNo().isBlank())
                wrapper.like("order_no", param.getOrderNo());
        }
        return baseService.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(OrderShipDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.SHIPPED.getCode());

        order.setOrderStatus(OrderStatus.SHIPPED.getCode());
        order.setShipTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("订单发货 orderId={}, logisticsNo={}, carrier={}", dto.getOrderId(), dto.getLogisticsNo(), dto.getCarrier());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrderCancelDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.CANCELLED.getCode());

        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(dto.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("订单取消 orderId={}, reason={}", dto.getOrderId(), dto.getCancelReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundApprove(OrderRefundDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        Integer current = order.getOrderStatus();

        if (dto.getApproved()) {
            // 退款通过：refunding(-2) → refunded(-3)
            OrderStateMachine.validate(current, OrderStatus.REFUNDED.getCode());
            order.setOrderStatus(OrderStatus.REFUNDED.getCode());
        } else {
            // 驳回：refunding(-2) → received(3)
            OrderStateMachine.validate(current, OrderStatus.RECEIVED.getCode());
            order.setOrderStatus(OrderStatus.RECEIVED.getCode());
            order.setCancelReason(dto.getRejectReason());
        }
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("退款审核 orderId={}, approved={}, reason={}", dto.getOrderId(), dto.getApproved(), dto.getRejectReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundDirect(OrderCancelDTO dto) {
        PurchaseOrder order = getOrderById(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.REJECTED.getCode());

        order.setOrderStatus(OrderStatus.REJECTED.getCode());
        order.setCancelReason(dto.getCancelReason());
        order.setRefundAuditTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("直接退款 orderId={}, reason={}", dto.getOrderId(), dto.getCancelReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            log.warn("paySuccess 订单不存在: {}", orderNo);
            return;
        }
        // 走状态机校验，而不是直接绕过
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("支付成功 orderNo={}", orderNo);
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        PurchaseOrder order = getOrderById(id);
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", id));
        Map<String, Object> map = BeanUtil.beanToMap(order);
        map.put("items", items);
        return map;
    }

    // ========== 私有方法 ==========

    private PurchaseOrder getOrderById(Long id) {
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        return order;
    }
}
