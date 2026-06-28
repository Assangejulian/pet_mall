package com.pat.order.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.ICartService;
import com.pat.order.service.IOrderItemService;
import com.pat.order.service.IPurchaseOrderService;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@Tag(name = "订单管理", description = "订单 CRUD、下单")
@RequestMapping("/api/order")
public class PurchaseOrderController extends BaseController<PurchaseOrder, PurchaseOrder, PurchaseOrder> {

    private final IPurchaseOrderService orderService;
    private final IProductService productService;
    private final ICartService cartService;
    private final IOrderItemService orderItemService;

    public PurchaseOrderController(IPurchaseOrderService service,
                                   IProductService productService,
                                   ICartService cartService,
                                   IOrderItemService orderItemService) {
        super(service);
        this.orderService = service;
        this.productService = productService;
        this.cartService = cartService;
        this.orderItemService = orderItemService;
    }

    @Override
    protected PurchaseOrder toVO(PurchaseOrder entity) {
        return entity;
    }

    @Override
    protected PurchaseOrder toDO(PurchaseOrder param) {
        return param;
    }

    @Override
    protected QueryWrapper<PurchaseOrder> buildQueryWrapper(PurchaseOrder param) {
        QueryWrapper<PurchaseOrder> wrapper = new QueryWrapper<>();
        if (param != null) {
            if (param.getUserId() != null) wrapper.eq("user_id", param.getUserId());
            if (param.getOrderStatus() != null) wrapper.eq("order_status", param.getOrderStatus());
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        List<OrderCreateDTO.OrderItemDTO> items = dto.getItems();

        // ① 校验商品 & 扣库存
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderCreateDTO.OrderItemDTO item : items) {
            Product product = productService.getById(item.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在: " + item.getProductId());
            }
            if (product.getStatus() != 1) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品已下架: " + product.getProductName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "库存不足: " + product.getProductName());
            }

            // 扣库存
            product.setStock(product.getStock() - item.getQuantity());
            productService.updateById(product);

            // 组装明细
            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getProductName());
            oi.setProductImage(product.getMainImage());
            oi.setPrice(product.getPrice());
            oi.setQuantity(item.getQuantity());
            orderItems.add(oi);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // ② 生成订单号（yyyyMMdd + 8位数字）
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randPart = String.format("%08d", new Random().nextInt(100000000));
        String orderNo = datePart + randPart;

        // ③ 创建订单
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(orderNo);
        order.setAddressId(dto.getAddressId());
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setOrderStatus(0);
        orderService.save(order);

        // ④ 写入订单明细
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItems);

        // ⑤ 清除已购商品的购物车
        List<Long> productIds = items.stream().map(OrderCreateDTO.OrderItemDTO::getProductId).collect(Collectors.toList());
        cartService.lambdaUpdate().in(Cart::getProductId, productIds).remove();

        return Result.success(order.getId());
    }
}