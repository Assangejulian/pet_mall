package com.pat.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.IPurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@Tag(name = "订单管理（后台）", description = "管理端订单查看、发货、取消、退单审核")
public class OrderAdminController {

    private final IPurchaseOrderService orderService;

    public OrderAdminController(IPurchaseOrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "订单分页查询")
    @GetMapping("/list")
    public Result<IPage<PurchaseOrder>> list(PurchaseOrder param, Page<PurchaseOrder> page) {
        QueryWrapper<PurchaseOrder> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (param != null) {
            if (param.getOrderStatus() != null) wrapper.eq("order_status", param.getOrderStatus());
            if (param.getUserId() != null) wrapper.eq("user_id", param.getUserId());
            if (param.getOrderNo() != null && !param.getOrderNo().isBlank())
                wrapper.like("order_no", param.getOrderNo());
        }
        return Result.success(orderService.page(page, wrapper));
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PurchaseOrder order = orderService.getById(id);
        if (order == null) return Result.error("订单不存在");

        Object statusObj = body.get("status");
        int status = statusObj instanceof Integer ? (Integer) statusObj : Integer.parseInt(statusObj.toString());
        order.setOrderStatus(status);

        String reason = (String) body.get("reason");
        LocalDateTime now = LocalDateTime.now();

        switch (status) {
            case -1: order.setCancelReason(reason); order.setCancelTime(now); break;
            case 1:  order.setPayTime(now); break;
            case 2:  order.setShipTime(now); break;
            case 3:  order.setReceiveTime(now); break;
            case 4:  order.setEvaluateTime(now); break;
        }

        return Result.success(orderService.updateById(order));
    }

    @Operation(summary = "退单审核")
    @PutMapping("/{id}/return-review")
    public Result<Boolean> reviewReturn(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PurchaseOrder order = orderService.getById(id);
        if (order == null) return Result.error("订单不存在");

        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        order.setOrderStatus(approved ? -3 : 3); // -3=退单通过, 3=已收货，拒绝后恢复
        order.setRefundAuditTime(LocalDateTime.now());

        if (!approved) {
            order.setCancelReason((String) body.get("reason"));
        }

        return Result.success(orderService.updateById(order));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        PurchaseOrder order = orderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("userId", order.getUserId());
        data.put("totalAmount", order.getTotalAmount());
        data.put("status", order.getOrderStatus());
        data.put("createTime", order.getCreateTime());
        data.put("payTime", order.getPayTime());
        data.put("cancelReason", order.getCancelReason());
        data.put("returnReason", order.getCancelReason());
        return Result.success(data);
    }
}