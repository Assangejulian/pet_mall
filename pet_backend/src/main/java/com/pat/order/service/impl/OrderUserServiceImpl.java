package com.pat.order.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.dto.OrderPaymentDTO;
import com.pat.order.domain.dto.OrderEvaluateDTO;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.ICartService;
import com.pat.order.service.IOrderUserService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.payment.service.PaymentServiceRouter;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.ProductService;
import com.pat.user.domain.entity.UserAddress;
import com.pat.user.mapper.UserAddressMapper;
import com.pat.common.util.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** 用户端订单操作：下单、查询 */
@Service
public class OrderUserServiceImpl implements IOrderUserService {

    private final PurchaseOrderBaseService baseService;
        private final ProductService productService;
    private final UserAddressMapper addressMapper;
    private final ICartService cartService;
    private final OrderItemMapper orderItemMapper;
    private final PaymentServiceRouter paymentServiceRouter;

    public OrderUserServiceImpl(PurchaseOrderBaseService baseService,
                                                                 ProductService productService,
                                UserAddressMapper addressMapper,
                                ICartService cartService,
                                OrderItemMapper orderItemMapper,
                                PaymentServiceRouter paymentServiceRouter) {
        this.baseService = baseService;
                this.productService = productService;
        this.addressMapper = addressMapper;
        this.cartService = cartService;
        this.orderItemMapper = orderItemMapper;
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
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderCreateDTO.OrderItemDTO item : items) {
            Product product = productService.getById(item.getProductId());
            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getProductName());
            oi.setProductImage(product.getMainImage());
            oi.setPrice(product.getPrice());
            oi.setQuantity(item.getQuantity());
            orderItems.add(oi);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 地址快照
        UserAddress addr = addressMapper.selectById(dto.getAddressId());
        if (addr == null) throw new BusinessException(ErrorCode.NOT_FOUND, "收货地址不存在");
        LinkedHashMap<String, String> snapshot = new LinkedHashMap<>();
        snapshot.put("receiverName", addr.getReceiverName());
        snapshot.put("phone", addr.getPhone());
        snapshot.put("province", addr.getProvince());
        snapshot.put("city", addr.getCity());
        snapshot.put("district", addr.getDistrict());
        snapshot.put("detail", addr.getDetail());

        // 创建订单
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(IdUtil.fastSimpleUUID());
        order.setUserId(userId);
        order.setAddressId(dto.getAddressId());
        order.setAddressSnapshot(JSONUtil.toJsonStr(snapshot));
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

        // 清购物车（已购商品）
        cartService.lambdaUpdate()
                .eq(Cart::getUserId, userId)
                .in(Cart::getProductId,
                        items.stream().map(OrderCreateDTO.OrderItemDTO::getProductId).collect(Collectors.toList()))
                .remove();

        return orderId;
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

        // 通过支付工厂路由到对应支付策略（mock / ALIPAY / WECHAT）
        return paymentServiceRouter.getService(dto.getPayMethod()).pay(order);
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

        order.setOrderStatus(OrderStatus.REJECTED.getCode());
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        baseService.updateById(order);
    }
}