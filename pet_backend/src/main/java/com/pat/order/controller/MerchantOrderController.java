package com.pat.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.common.util.UserHolder;
import com.pat.order.domain.dto.OrderShipDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.IOrderQueryService;
import com.pat.order.service.IOrderShipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/order")
@Tag(name = "订单管理（商家端）", description = "商家查看自己店铺的订单、发货")
public class MerchantOrderController {

    private final IOrderQueryService orderQueryService;
    private final IOrderShipService orderShipService;

    public MerchantOrderController(IOrderQueryService orderQueryService,
                                   IOrderShipService orderShipService) {
        this.orderQueryService = orderQueryService;
        this.orderShipService = orderShipService;
    }

    @Operation(summary = "商家订单分页查询")
    @GetMapping("/search")
    public Result<IPage<Map<String, Object>>> list(Integer orderStatus, Page<?> page) {
        PurchaseOrder param = new PurchaseOrder();
        param.setOrderStatus(orderStatus);
        return Result.success(orderQueryService.pageList(param, page, UserHolder.getUserId()));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(orderQueryService.getDetail(id, UserHolder.getUserId()));
    }

    @Operation(summary = "发货")
    @PutMapping("/ship")
    public Result<Void> ship(@Valid @RequestBody OrderShipDTO dto) {
        orderShipService.shipOrder(dto, UserHolder.getUserId());
        return Result.success();
    }
}
