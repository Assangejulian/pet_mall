package com.pat.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.common.util.UserHolder;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.dto.OrderEvaluateDTO;
import com.pat.order.domain.dto.OrderPaymentDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.ICartService;
import com.pat.order.service.IOrderUserService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.order.service.support.OrderAmountCalculator;
import com.pat.order.service.support.OrderPersistenceService;
import com.pat.order.service.support.OrderProductService;
import com.pat.order.service.support.OrderStateService;
import com.pat.payment.domain.dto.PaymentContext;
import com.pat.payment.domain.vo.OrderPaymentVO;
import com.pat.payment.service.impl.PaymentServiceRouter;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户端订单操作 —— 编排层。
 *
 * <p>职责是编排各领域 Service 完成订单业务流程，自身不包含具体业务实现。
 * 每个 public 方法对应一个用户操作意图（下单、支付、取消等），编排粒度与用例一一对应。</p>
 */
@Slf4j
@Service
public class OrderUserServiceImpl implements IOrderUserService, com.pat.payment.service.PaymentCallback {

    private final OrderProductService orderProductService;
    private final OrderAmountCalculator amountCalculator;
    private final OrderPersistenceService orderPersistenceService;
    private final OrderStateService orderStateService;
    private final UserService userService;
    private final ICartService cartService;
    private final OrderItemMapper orderItemMapper;
    private final PurchaseOrderBaseService baseService;
    private final PaymentServiceRouter paymentServiceRouter;

    public OrderUserServiceImpl(OrderProductService orderProductService,
                                OrderAmountCalculator amountCalculator,
                                OrderPersistenceService orderPersistenceService,
                                OrderStateService orderStateService,
                                UserService userService,
                                ICartService cartService,
                                OrderItemMapper orderItemMapper,
                                PurchaseOrderBaseService baseService,
                                PaymentServiceRouter paymentServiceRouter) {
        this.orderProductService = orderProductService;
        this.amountCalculator = amountCalculator;
        this.orderPersistenceService = orderPersistenceService;
        this.orderStateService = orderStateService;
        this.userService = userService;
        this.cartService = cartService;
        this.orderItemMapper = orderItemMapper;
        this.baseService = baseService;
        this.paymentServiceRouter = paymentServiceRouter;
    }

    // ===================== 内部工具方法 =====================

    private static Long requireUserId() {
        Long uid = UserHolder.getUserId();
        if (uid == null) throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "用户未登录");
        return uid;
    }

    /** 获取当前用户拥有的订单（校验归属权）。 */
    private PurchaseOrder getOwnedOrder(Long id) {
        Long userId = requireUserId();
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        if (!userId.equals(order.getUserId())) throw new BusinessException(ErrorCode.FARAMS_ERROR, "无权查看");
        return order;
    }

    // ===================== 编排方法 =====================

    /**
     * 创建订单（下单）。
     *
     * <p>编排 5 个步骤：校验商品并扣库存 → 地址快照 → 计算金额 → 保存订单 → 清购物车。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO dto) {
        Long userId = requireUserId();

        var validated = orderProductService.validateAndDeduct(dto.getItems());
        String addressSnapshot = orderPersistenceService.snapshotAddressById(dto.getAddressId());

        User user = userService.getById(userId);
        var amount = amountCalculator.calculate(validated.getTotal(), user.getMemberLevel());

        Long orderId = orderPersistenceService.save(userId, dto, validated.getOrderItems(), addressSnapshot,
                amount.getTotalAmount(), amount.getDiscountAmount(), amount.getPayAmount());

        cartService.cleanByProductIds(userId,
                dto.getItems().stream().map(OrderCreateDTO.OrderItemDTO::getProductId).collect(Collectors.toList()));

        return orderId;
    }

    // ===================== 查询 =====================

    @Override
    public IPage<PurchaseOrder> getUserOrderList(Integer orderStatus, Page<PurchaseOrder> page) {
        Long userId = requireUserId();
        QueryWrapper<PurchaseOrder> wrapper = new QueryWrapper<PurchaseOrder>()
                .eq("user_id", userId).orderByDesc("create_time");
        if (orderStatus != null) wrapper.eq("order_status", orderStatus);
        return baseService.page(page, wrapper);
    }

    @Override
    public PurchaseOrder getUserOrderDetail(Long id) {
        PurchaseOrder order = getOwnedOrder(id);
        order.setItems(orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId())));
        return order;
    }

    @Override
    public List<OrderItem> getUserOrderItems(Long orderId) {
        PurchaseOrder order = getOwnedOrder(orderId);
        return orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
    }

    // ===================== 支付 =====================

    /**
     * 支付订单。
     *
     * <p>校验订单状态后，通过 {@link PaymentServiceRouter} 路由到对应的支付渠道。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentVO payOrder(OrderPaymentDTO dto) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, dto.getOrderNo())
                .one();
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());

        PaymentContext ctx = new PaymentContext();
        ctx.setOrderNo(order.getOrderNo());
        ctx.setTotalAmount(order.getPayAmount());
        ctx.setSubject("宠物商城订单");
        ctx.setDescription("订单" + order.getOrderNo());

        return paymentServiceRouter.getService(dto.getPayMethod()).pay(ctx);
    }

    // ===================== 状态变更 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder confirmReceive(Long id) {
        PurchaseOrder order = getOwnedOrder(id);
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.RECEIVED.getCode());
        orderStateService.receive(order);
        return order;
    }

    /**
     * 评价订单。
     *
     * <p>先逐项更新订单明细的评价内容，再推进订单状态到已评价。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateOrder(OrderEvaluateDTO dto) {
        PurchaseOrder order = getOwnedOrder(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.EVALUATED.getCode());

        List<OrderItem> existingItems = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        Map<Long, OrderItem> itemMap = existingItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item));
        LocalDateTime now = LocalDateTime.now();
        if (dto.getItems() != null) {
            for (OrderEvaluateDTO.ItemEvaluate ie : dto.getItems()) {
                OrderItem item = itemMap.get(ie.getOrderItemId());
                if (item != null) {
                    item.setEvaluateContent(ie.getContent());
                    item.setEvaluateTime(now);
                    orderItemMapper.updateById(item);
                }
            }
        }
        orderStateService.evaluate(order, now);
    }

    /**
     * 取消订单。
     *
     * <p>先恢复库存，再变更订单状态。支持待支付和已支付状态的订单取消。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        PurchaseOrder order = getOwnedOrder(orderId);
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.CANCELLED.getCode());

        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));
        orderProductService.restoreStock(items);

        orderStateService.cancel(order, reason, "user");
        log.info("用户取消订单 orderId={}, reason={}", orderId, reason);
    }

    /**
     * 申请退款。
     *
     * <p>保存退款前状态（preRefundStatus）用于管理员驳回时恢复，将订单推进到退款审核中状态。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long orderId, String reason) {
        Long userId = requireUserId();
        PurchaseOrder order = baseService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.REFUNDING.getCode());
        orderStateService.applyRefund(order, reason);
    }

    // ===================== 支付回调 =====================

    /**
     * 支付成功回调。
     *
     * <p>由支付渠道异步通知触发。包含幂等校验：已支付的订单直接跳过，防止重复回调导致异常。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onPaymentSuccess(String orderNo, java.math.BigDecimal amount, LocalDateTime payTime) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            log.warn("支付回调订单不存在 {}", orderNo);
            return;
        }
        if (OrderStatus.PAID.getCode() == order.getOrderStatus()) {
            log.info("支付回调忽略，订单已支付 orderNo={}", orderNo);
            return;
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        orderStateService.paySuccess(order, payTime != null ? payTime : LocalDateTime.now());
        log.info("支付回调更新订单状态成功 orderNo={}", orderNo);
    }
}