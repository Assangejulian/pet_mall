package com.pat.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.dto.OrderPaymentDTO;
import com.pat.order.domain.dto.OrderEvaluateDTO;
import com.pat.order.domain.vo.OrderCreateVO;
import com.pat.payment.domain.vo.OrderPaymentVO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.IOrderUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理（用户端）", description = "用户订单查询与创建")
public class PurchaseOrderController {

    private final IOrderUserService orderUserService;

    public PurchaseOrderController(IOrderUserService orderUserService) {
        this.orderUserService = orderUserService;
    }

    @Operation(summary = "下单")
    @PostMapping("/create")
    public Result<OrderCreateVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        Long orderId = orderUserService.createOrder(dto);
        return Result.success(new OrderCreateVO(orderId));
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

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/receive")
    public Result<PurchaseOrder> receive(@PathVariable Long id) {
        return Result.success(orderUserService.confirmReceive(id));
    }

    @Operation(summary = "订单商品明细")
    @GetMapping("/item/search")
    public Result<List<OrderItem>> items(Long orderId) {
        return Result.success(orderUserService.getUserOrderItems(orderId));
    }

    @Operation(summary = "评价订单")
    @PostMapping("/evaluate")
    public Result<Void> evaluate(@Valid @RequestBody OrderEvaluateDTO dto) {
        orderUserService.evaluateOrder(dto);
        return Result.success();
    }

    @Operation(summary = "极速退款")
    @PostMapping("/{id}/refund_direct")
    public Result<Void> refundDirect(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "用户发起极速退款");
        orderUserService.directRefund(id, reason);
        return Result.success();
    }

    @Operation(summary = "申请退单")
    @PostMapping("/{id}/refund_apply")
    public Result<Void> refundApply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "用户申请退款");
        orderUserService.applyRefund(id, reason);
        return Result.success();
    }
}
