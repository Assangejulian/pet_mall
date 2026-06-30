package com.pat.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.dto.OrderPaymentDTO;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.IOrderUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理（用户端）", description = "用户订单查询与创建")
public class PurchaseOrderController {

    private final IOrderUserService orderUserService;

    public PurchaseOrderController(IOrderUserService orderUserService) {
        this.orderUserService = orderUserService;
    }

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<Long> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderUserService.createOrder(dto));
    }

    @Operation(summary = "当前用户订单列表")
    @GetMapping("/search")
    public Result<IPage<PurchaseOrder>> search(Integer orderStatus, Page<PurchaseOrder> page) {
        return Result.success(orderUserService.getUserOrderList(orderStatus, page));
    }

    @Operation(summary = "订单详情（含明细）")
    @GetMapping("/{id}")
    public Result<PurchaseOrder> detail(@PathVariable Long id) {
        return Result.success(orderUserService.getUserOrderDetail(id));
    }

    @Operation(summary = "支付订单（模拟）")
    @PostMapping("/pay")
    public Result<OrderPaymentVO> pay(@Valid @RequestBody OrderPaymentDTO dto) {
        return Result.success(orderUserService.payOrder(dto));
    }

    @Operation(summary = "订单商品明细")
    @GetMapping("/item/search")
    public Result<List<OrderItem>> items(Long orderId) {
        return Result.success(orderUserService.getUserOrderItems(orderId));
    }
}
