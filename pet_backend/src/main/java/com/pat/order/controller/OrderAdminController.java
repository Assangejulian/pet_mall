package com.pat.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.order.domain.dto.OrderCancelDTO;
import com.pat.order.domain.dto.OrderRefundDTO;
import com.pat.order.domain.dto.OrderShipDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.IOrderAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@Tag(name = "订单管理（后台）", description = "管理端订单查看、发货、取消、退款审核")
public class OrderAdminController {

    private final IOrderAdminService orderAdminService;

    public OrderAdminController(IOrderAdminService orderAdminService) {
        this.orderAdminService = orderAdminService;
    }

    @Operation(summary = "订单分页查询")
    @GetMapping("/search")
    public Result<IPage<PurchaseOrder>> list(PurchaseOrder param, Page<PurchaseOrder> page) {
        return Result.success(orderAdminService.pageList(param, page));
    }

    @Operation(summary = "发货")
    @PutMapping("/ship")
    public Result<Void> ship(@Valid @RequestBody OrderShipDTO dto) {
        orderAdminService.shipOrder(dto);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody OrderCancelDTO dto) {
        orderAdminService.cancelOrder(dto);
        return Result.success();
    }

    @Operation(summary = "退款审核（通过/驳回）")
    @PutMapping("/refund/approve")
    public Result<Void> refundApprove(@Valid @RequestBody OrderRefundDTO dto) {
        orderAdminService.refundApprove(dto);
        return Result.success();
    }

    @Operation(summary = "直接退款（不经过申请流程）")
    @PutMapping("/refund/direct")
    public Result<Void> refundDirect(@Valid @RequestBody OrderCancelDTO dto) {
        orderAdminService.refundDirect(dto);
        return Result.success();
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(orderAdminService.getDetail(id));
    }
}
