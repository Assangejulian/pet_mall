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
import com.pat.order.service.ICartService;
import com.pat.order.service.IOrderItemService;
import com.pat.order.service.IOrderUserService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.order.service.support.OrderAmountCalculator;
import com.pat.order.service.support.OrderCreationService;
import com.pat.order.service.support.OrderProductService;
import com.pat.order.service.support.OrderEvaluateService;
import com.pat.order.service.support.OrderStatusUpdater;
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
import java.util.stream.Collectors;

/**
 * 用户端订单操作 —— 编排层。
 *
 * <p>职责是编排各领域 Service 完成订单业务流程，自身不包含具体业务实现。
 * 每个 public 方法对应一个用户操作意图（下单、支付、取消等），编排粒度与用例一一对应。</p>
 */
@Slf4j
@Service
public class CustomerOrderServiceImpl implements IOrderUserService, com.pat.payment.service.PaymentCallback {

    private final OrderProductService orderProductService;
    private final IOrderItemService orderItemService;
    private final OrderAmountCalculator amountCalculator;
    private final OrderCreationService orderCreationService;
    private final OrderEvaluateService orderEvaluateService;
    private final OrderStatusUpdater orderStatusUpdater;
    private final UserService userService;
    private final ICartService cartService;
    private final PurchaseOrderBaseService baseService;
    private final PaymentServiceRouter paymentServiceRouter;

    public CustomerOrderServiceImpl(OrderProductService orderProductService,
                                    IOrderItemService orderItemService,
                                    OrderAmountCalculator amountCalculator,
                                    OrderCreationService orderCreationService,
                                    OrderStatusUpdater orderStatusUpdater,
                                    OrderEvaluateService orderEvaluateService,
                                    UserService userService,
                                    ICartService cartService,
                                    PurchaseOrderBaseService baseService,
                                    PaymentServiceRouter paymentServiceRouter) {
        this.orderProductService = orderProductService;
        this.orderItemService = orderItemService;
        this.amountCalculator = amountCalculator;
        this.orderCreationService = orderCreationService;
        this.orderStatusUpdater = orderStatusUpdater;
        this.orderEvaluateService = orderEvaluateService;
        this.userService = userService;
        this.cartService = cartService;
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
        String addressSnapshot = orderCreationService.snapshotAddressById(dto.getAddressId(), userId);

        User user = userService.getById(userId);
        var amount = amountCalculator.calculate(validated.getTotal(), user.getMemberLevel());

        Long orderId = orderCreationService.save(userId, dto, validated.getOrderItems(), addressSnapshot,
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
        IPage<PurchaseOrder> result = baseService.page(page, wrapper);

        // 批量加载每笔订单的商品明细
        List<PurchaseOrder> orders = result.getRecords();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream().map(PurchaseOrder::getId).collect(Collectors.toList());
            List<OrderItem> allItems = orderItemService.lambdaQuery()
                    .in(OrderItem::getOrderId, orderIds).list();
            java.util.Map<Long, List<OrderItem>> itemMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));
            orders.forEach(o -> o.setItems(itemMap.getOrDefault(o.getId(), java.util.Collections.emptyList())));
        }

        return result;
    }

    @Override
    public PurchaseOrder getUserOrderDetail(Long id) {
        PurchaseOrder order = getOwnedOrder(id);
        order.setItems(orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId()).list());
        return order;
    }

    @Override
    public List<OrderItem> getUserOrderItems(Long orderId) {
        PurchaseOrder order = getOwnedOrder(orderId);
        return orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId()).list();
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
        orderStatusUpdater.receive(order);
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

        LocalDateTime now = orderEvaluateService.updateEvaluations(order.getId(), dto.getItems());
        orderStatusUpdater.evaluate(order, now);
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

        orderProductService.restoreStockByOrderId(orderId);

        orderStatusUpdater.cancel(order, reason, "user");
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
        orderStatusUpdater.applyRefund(order, reason);
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
        orderStatusUpdater.paySuccess(order, payTime != null ? payTime : LocalDateTime.now());
        log.info("支付回调更新订单状态成功 orderNo={}", orderNo);
    }
}