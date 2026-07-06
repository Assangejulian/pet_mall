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
import com.pat.order.service.OrderAmountCalculator;
import com.pat.order.service.OrderPersistenceService;
import com.pat.order.service.OrderProductService;
import com.pat.order.service.OrderStateService;
import com.pat.order.service.base.PurchaseOrderBaseService;
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

    private static Long requireUserId() {
        Long uid = UserHolder.getUserId();
        if (uid == null) throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "用户未登录");
        return uid;
    }

    private PurchaseOrder getOwnedOrder(Long id) {
        Long userId = requireUserId();
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        if (!userId.equals(order.getUserId())) throw new BusinessException(ErrorCode.FARAMS_ERROR, "无权查看");
        return order;
    }

    // =================================================================
    //  编排：每行一个高层面意图
    // =================================================================

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

    // =================================================================
    //  查询
    // =================================================================

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

    // =================================================================
    //  支付
    // =================================================================

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

    // =================================================================
    //  状态变更（编排 + 委托 OrderStateService 执行）
    // =================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder confirmReceive(Long id) {
        PurchaseOrder order = getOwnedOrder(id);
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.RECEIVED.getCode());
        orderStateService.receive(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateOrder(OrderEvaluateDTO dto) {
        PurchaseOrder order = getOwnedOrder(dto.getOrderId());
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.EVALUATED.getCode());

        // 逐项更新评价内容（业务特殊，保留在编排层）
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