package com.pat.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.common.utils.UserHolder;
import com.pat.order.dto.OrderSubmitDTO;
import com.pat.order.entity.Cart;
import com.pat.order.entity.OrderItem;
import com.pat.order.entity.PurchaseOrder;
import com.pat.order.mapper.PurchaseOrderMapper;
import com.pat.order.service.ICartService;
import com.pat.order.service.IOrderItemService;
import com.pat.order.service.IPurchaseOrderService;
import com.pat.product.entity.Product;
import com.pat.product.service.ProductService;
import com.pat.user.entity.UserAddress;
import com.pat.user.service.IUserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements IPurchaseOrderService {

    private final ICartService cartService;
    private final IOrderItemService orderItemService;
    private final IUserAddressService userAddressService;
    private final ProductService productService;

    public PurchaseOrderServiceImpl(ICartService cartService, IOrderItemService orderItemService,
                                    IUserAddressService userAddressService, ProductService productService) {
        this.cartService = cartService;
        this.orderItemService = orderItemService;
        this.userAddressService = userAddressService;
        this.productService = productService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder createOrderFromCart(OrderSubmitDTO dto) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.Not_AUTH);
        }

        UserAddress address = userAddressService.getById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "无效的收货地址");
        }

        LambdaQueryWrapper<Cart> cartQuery = new LambdaQueryWrapper<>();
        cartQuery.eq(Cart::getUserId, userId);
        if (dto.getCartIds() != null && !dto.getCartIds().isEmpty()) {
            cartQuery.in(Cart::getId, dto.getCartIds());
        } else {
            cartQuery.eq(Cart::getChecked, 1);
        }
        List<Cart> cartItems = cartService.list(cartQuery);
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "购物车为空或未选中商品");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Cart cart : cartItems) {
            Product product = productService.getById(cart.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "部分商品已下架或不存在");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品 [" + product.getProductName() + "] 库存不足");
            }
            
            boolean stockDeducted = productService.deductStock(product.getId(), cart.getQuantity());
            if (!stockDeducted) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品 [" + product.getProductName() + "] 库存扣减失败，可能由于并发购买");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItems.add(orderItem);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setOrderStatus(0); 

        save(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItems);

        List<Long> cartIdsToRemove = cartItems.stream().map(Cart::getId).collect(Collectors.toList());
        cartService.removeByIds(cartIdsToRemove);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long orderId, Integer targetStatus, String reason) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        Integer currentStatus = order.getOrderStatus();
        LocalDateTime now = LocalDateTime.now();

        if (targetStatus == 1) { 
            if (currentStatus != 0) throw new BusinessException(ErrorCode.FARAMS_ERROR, "只能对待支付订单进行支付");
            order.setPayTime(now);
        } else if (targetStatus == 2) { 
            if (currentStatus != 1) throw new BusinessException(ErrorCode.FARAMS_ERROR, "只能对已支付订单进行发货");
            order.setShipTime(now);
        } else if (targetStatus == 3) { 
            if (currentStatus != 2) throw new BusinessException(ErrorCode.FARAMS_ERROR, "只能对已发货订单进行收货确认");
            order.setReceiveTime(now);
        } else if (targetStatus == 4) { 
            if (currentStatus != 3) throw new BusinessException(ErrorCode.FARAMS_ERROR, "只能对已收货订单进行评价");
            order.setEvaluateTime(now);
        } else if (targetStatus == -1) { 
            if (currentStatus != 0) throw new BusinessException(ErrorCode.FARAMS_ERROR, "只能取消待支付订单");
            order.setCancelTime(now);
            order.setCancelReason(reason);
        } else if (targetStatus == -2) { 
            if (currentStatus != 2 && currentStatus != 3) throw new BusinessException(ErrorCode.FARAMS_ERROR, "当前状态不能申请退单");
            order.setRefundApplyTime(now);
            order.setCancelReason(reason);
        } else if (targetStatus == -3 || targetStatus == -4) { 
            if (currentStatus != -2) throw new BusinessException(ErrorCode.FARAMS_ERROR, "没有申请退单，无法审核");
            order.setRefundAuditTime(now);
        } else {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "未知的目标状态");
        }

        order.setOrderStatus(targetStatus);
        return updateById(order);
    }
}