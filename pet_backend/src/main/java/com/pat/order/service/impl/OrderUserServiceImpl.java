package com.pat.order.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.dto.OrderPaymentDTO;
import com.pat.order.domain.dto.OrderEvaluateDTO;
import com.pat.payment.domain.vo.OrderPaymentVO;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.ICartService;
import com.pat.order.service.IOrderUserService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.payment.domain.dto.PaymentContext;
import com.pat.payment.domain.dto.PaymentNotify;
import com.pat.payment.service.PaymentCallback;
import com.pat.payment.service.PaymentService;
import com.pat.payment.service.impl.PaymentServiceRouter;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.ProductService;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.user.domain.entity.UserAddress;
import com.pat.user.mapper.UserAddressMapper;
import com.pat.user.service.UserService;
import com.pat.common.util.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** 用户端订单操作：下单、查询 */
@Slf4j
@Service
public class OrderUserServiceImpl implements IOrderUserService, PaymentCallback {

    private final PurchaseOrderBaseService baseService;
    private final ProductService productService;
    private final IStoreService storeService;
    private final UserService userService;
    private final UserAddressMapper addressMapper;
    private final ICartService cartService;
    private final OrderItemMapper orderItemMapper;
    private final PaymentServiceRouter paymentServiceRouter;

    public OrderUserServiceImpl(PurchaseOrderBaseService baseService,
                                ProductService productService,
                                IStoreService storeService,
                                UserAddressMapper addressMapper,
                                ICartService cartService,
                                OrderItemMapper orderItemMapper,
                                PaymentServiceRouter paymentServiceRouter,
                                UserService userService) {
        this.baseService = baseService;
        this.productService = productService;
        this.storeService = storeService;
        this.addressMapper = addressMapper;
        this.cartService = cartService;
        this.orderItemMapper = orderItemMapper;
        this.paymentServiceRouter = paymentServiceRouter;
        this.userService = userService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 创建订单。行级锁扣库存、地址快照、生成订单号、清购物车。
     *
     * @param dto 下单参数
     * @return 新订单 ID
     */
    public Long createOrder(OrderCreateDTO dto) {
        Long userId = requireUserId();
        List<OrderCreateDTO.OrderItemDTO> items = dto.getItems();

        // 1. 验证商品并扣库存
        OrderItemsResult itemsResult = buildOrderItems(items);

        // 2. 地址快照
        String addressSnapshot = buildAddressSnapshot(dto.getAddressId(), userId);

        // 3. 保存订单 + 明细
        Long orderId = saveOrder(userId, dto, itemsResult.getOrderItems(), itemsResult.getTotal(), addressSnapshot);

        // 4. 清购物车
        clearCart(userId, items);

        return orderId;
    }

    // ==================== createOrder 辅助方法 ====================

    private OrderItemsResult buildOrderItems(List<OrderCreateDTO.OrderItemDTO> items) {
        List<OrderItem> orderItems = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        Long orderStoreId = null;
        for (OrderCreateDTO.OrderItemDTO item : items) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "购买数量必须大于0");
            }
            Product product = productService.getById(item.getProductId());
            if (product == null) throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
            if (product.getStatus() == null || product.getStatus() != 1)
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品已下架: " + product.getProductName());
            if (orderStoreId == null) {
                orderStoreId = product.getStoreId();
            } else if (!Objects.equals(orderStoreId, product.getStoreId())) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "一张订单只能购买同一门店的商品，请分开结算");
            }
            products.add(product);
        }
        requireOperatingStore(orderStoreId);

        for (int index = 0; index < items.size(); index++) {
            OrderCreateDTO.OrderItemDTO item = items.get(index);
            Product product = products.get(index);
            boolean stockOk = productService.deductStock(product.getId(), item.getQuantity());
            if (!stockOk) throw new BusinessException(500, "商品库存不足或已下架: " + product.getProductName(), null);
            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getProductName());
            oi.setProductImage(product.getMainImage());
            oi.setPrice(product.getPrice());
            oi.setQuantity(item.getQuantity());
            orderItems.add(oi);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return new OrderItemsResult(orderItems, total);
    }

    private String buildAddressSnapshot(Long addressId, Long userId) {
        UserAddress addr = addressMapper.selectById(addressId);
        if (addr == null) throw new BusinessException(ErrorCode.NOT_FOUND, "收货地址不存在");
        if (!userId.equals(addr.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权使用该收货地址");
        }
        LinkedHashMap<String, String> snapshot = new LinkedHashMap<>();
        snapshot.put("receiverName", addr.getReceiverName());
        snapshot.put("phone", addr.getPhone());
        snapshot.put("province", addr.getProvince());
        snapshot.put("city", addr.getCity());
        snapshot.put("district", addr.getDistrict());
        snapshot.put("detail", addr.getDetail());
        return JSONUtil.toJsonStr(snapshot);
    }

    private Long saveOrder(Long userId, OrderCreateDTO dto, List<OrderItem> orderItems, BigDecimal total, String addressSnapshot) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(IdUtil.fastSimpleUUID());
        order.setUserId(userId);
        order.setAddressId(dto.getAddressId());
        order.setAddressSnapshot(addressSnapshot);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setRemark(dto.getRemark());
        order.setOrderStatus(OrderStatus.PENDING_PAY.getCode());
        baseService.save(order);
        Long orderId = order.getId();
        for (OrderItem oi : orderItems) {
            oi.setOrderId(orderId);
            orderItemMapper.insert(oi);
        }
        return orderId;
    }

    private void clearCart(Long userId, List<OrderCreateDTO.OrderItemDTO> items) {
        cartService.lambdaUpdate()
                .eq(Cart::getUserId, userId)
                .in(Cart::getProductId,
                        items.stream().map(OrderCreateDTO.OrderItemDTO::getProductId).collect(Collectors.toList()))
                .remove();
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    private static class OrderItemsResult {
        private List<OrderItem> orderItems;
        private BigDecimal total;
    }

    @Override
    /**
     * 获取当前用户的订单列表（分页），附带商品明细。
     *
     * @param orderStatus 状态筛选
     * @param page 分页参数
     * @return 分页订单
     */
    public IPage<PurchaseOrder> getUserOrderList(Integer orderStatus, Page<PurchaseOrder> page) {
        Long userId = requireUserId();
        QueryWrapper<PurchaseOrder> wrapper = new QueryWrapper<PurchaseOrder>()
                .eq("user_id", userId).orderByDesc("create_time");
        if (orderStatus != null) {
            if (orderStatus == -99) {
                wrapper.in("order_status", java.util.Arrays.asList(-2, -3, -4));
            } else {
                wrapper.eq("order_status", orderStatus);
            }
        }
        IPage<PurchaseOrder> result = baseService.page(page, wrapper);
        // 批量加载每个订单的商品明细
        for (PurchaseOrder order : result.getRecords()) {
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
            order.setItems(orderItems);
        }
        return result;
    }

    @Override
    /**
     * 获取当前用户某笔订单详情。
     *
     * @param id 订单 ID
     * @return 订单详情
     */
    public PurchaseOrder getUserOrderDetail(Long id) {
        PurchaseOrder order = getOwnedOrder(id);
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        order.setItems(items);
        return order;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 支付订单。路由到具体支付策略。
     *
     * @param dto 支付参数
     * @return 支付结果 VO
     */
    public OrderPaymentVO payOrder(OrderPaymentDTO dto) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, dto.getOrderNo())
                .one();
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        Long userId = requireUserId();
        if (!userId.equals(order.getUserId())) throw new BusinessException(ErrorCode.FARAMS_ERROR, "无权操作");

        // 构建支付上下文，传给支付模块
        PaymentContext ctx = new PaymentContext();
        ctx.setOrderNo(order.getOrderNo());
        ctx.setTotalAmount(order.getPayAmount());
        ctx.setSubject("宠物商城 - " + order.getOrderNo());
        ctx.setDescription("宠物商城订单支付");
        // 微信支付需要 openid，从当前用户中获取
        com.pat.user.domain.entity.User currentUser = userService.getById(UserHolder.getUserId());
        ctx.setOpenid(currentUser != null ? currentUser.getOpenid() : null);

        PaymentService svc = paymentServiceRouter.getService(dto.getPayMethod());
        OrderPaymentVO vo = svc.pay(ctx);

        // 模拟支付同步回调确认支付
        if ("mock".equals(dto.getPayMethod())) {
            PaymentNotify notify = new PaymentNotify();
            notify.setOutTradeNo(order.getOrderNo());
            svc.handleNotify(notify, this);
            vo.setStatus(OrderStatus.PAID.getCode());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 确认收货。状态机校验后置为已收货。
     *
     * @param id 订单 ID
     * @return 更新后的订单
     */
    public PurchaseOrder confirmReceive(Long id) {
        PurchaseOrder order = getOwnedOrder(id);
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.RECEIVED.getCode());
        order.setOrderStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        baseService.updateById(order);
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        order.setItems(items);
        return order;
    }

    @Override
    /**
     * 获取当前用户某笔订单的商品明细。
     *
     * @param orderId 订单 ID
     * @return 商品明细列表
     */
    public List<OrderItem> getUserOrderItems(Long orderId) {
        PurchaseOrder order = getOwnedOrder(orderId);
        return orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
    }

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
        
        order.setOrderStatus(4);
        order.setEvaluateTime(now);
        baseService.updateById(order);
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

        order.setOrderStatus(OrderStatus.REFUNDING.getCode());
        order.setCancelReason(reason);
        order.setRefundApplyTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void directRefund(Long orderId, String reason) {
        Long userId = requireUserId();
        PurchaseOrder order = baseService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.REJECTED.getCode());

        restoreOrderStock(order.getId());
        order.setOrderStatus(OrderStatus.REJECTED.getCode());
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        baseService.updateById(order);
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

    private void requireOperatingStore(Long storeId) {
        if (storeId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品所属门店不存在");
        }
        Store store = storeService.getById(storeId);
        if (store == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品所属门店不存在");
        }
        if (store.getDeleted() != null && store.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品所属门店已删除，不能下单");
        }
        if (!Integer.valueOf(1).equals(store.getStatus())) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品所属门店未营业，不能下单");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onPaymentSuccess(String orderNo, java.math.BigDecimal amount, java.time.LocalDateTime payTime) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            log.warn("支付回调订单不存在: {}", orderNo);
            return;
        }
        // 已支付则直接返回（防止第三方重复回调导致异常）
        if (OrderStatus.PAID.getCode() == order.getOrderStatus()) {
            log.info("支付回调忽略，订单已支付 orderNo={}", orderNo);
            return;
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(payTime != null ? payTime : java.time.LocalDateTime.now());
        baseService.updateById(order);
        log.info("支付回调更新订单状态成功 orderNo={}", orderNo);
    }

}
