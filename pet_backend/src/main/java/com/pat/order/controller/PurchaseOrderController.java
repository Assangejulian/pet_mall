package com.pat.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.Result;
import com.pat.common.utils.UserHolder;
import com.pat.order.dto.OrderSubmitDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.IPurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "订单管理", description = "订单 CRUD")
@RequestMapping("/api/order")
public class PurchaseOrderController extends BaseController<PurchaseOrder, PurchaseOrder, PurchaseOrder> {

    private final IPurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(IPurchaseOrderService service) {
        super(service);
        this.purchaseOrderService = service;
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
        // 区分管理员和普通用户
        if (!"admin".equals(UserHolder.getRole())) {
            wrapper.eq("user_id", UserHolder.getUserId());
        }
        return wrapper;
    }

    // 覆盖默认的 save 方法，或者定义一个新的接口。由于默认 save 接收 P(PurchaseOrder)，
    // 我们自定义一个专门接收 OrderSubmitDTO 的接口
    @PostMapping("/create")
    public Result<PurchaseOrder> createOrder(@RequestBody @Valid OrderSubmitDTO dto) {
        PurchaseOrder order = purchaseOrderService.createOrderFromCart(dto);
        return Result.success(order);
    }

    // 状态流转
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, 
                                        @RequestParam Integer status, 
                                        @RequestParam(required = false) String reason) {
        boolean success = purchaseOrderService.updateOrderStatus(id, status, reason);
        return Result.success(success);
    }
}