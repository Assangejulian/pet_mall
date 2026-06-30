package com.pat.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.IOrderAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@Tag(name = "订单管理（后台）", description = "管理端订单查看、发货、取消、退单审核")
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

    @Operation(summary = "更新订单状态 / 退单审核")
    @PutMapping("/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        orderAdminService.updateStatus(id, body);
        return Result.success();
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(orderAdminService.getDetail(id));
    }
}
