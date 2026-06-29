package com.pat.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.Result;
import com.pat.common.utils.UserHolder;
import com.pat.order.dto.OrderSubmitDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.vo.PurchaseOrderVO;
import com.pat.order.vo.OrderItemVO;
import com.pat.order.service.IPurchaseOrderService;
import com.pat.order.service.IOrderItemService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "订单管理", description = "订单 CRUD")
@RequestMapping("/api/order")
public class PurchaseOrderController extends BaseController<PurchaseOrder, PurchaseOrder, PurchaseOrderVO> {

    private final IPurchaseOrderService purchaseOrderService;
    private final IOrderItemService orderItemService;

    public PurchaseOrderController(IPurchaseOrderService service, IOrderItemService orderItemService) {
        super(service);
        this.purchaseOrderService = service;
        this.orderItemService = orderItemService;
    }

    @Override
    protected PurchaseOrderVO toVO(PurchaseOrder entity) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        BeanUtils.copyProperties(entity, vo);
        
        List<OrderItem> items = orderItemService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, entity.getId())
        );
        
        List<OrderItemVO> itemVOs = items.stream().map(i -> {
            OrderItemVO ivo = new OrderItemVO();
            BeanUtils.copyProperties(i, ivo);
            return ivo;
        }).collect(Collectors.toList());
        
        vo.setItems(itemVOs);
        return vo;
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
        
        if (param.getOrderStatus() != null) {
            wrapper.eq("order_status", param.getOrderStatus());
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }

    @PostMapping("/create")
    public Result<PurchaseOrderVO> createOrder(@RequestBody @Valid OrderSubmitDTO dto) {
        PurchaseOrder order = purchaseOrderService.createOrderFromCart(dto);
        return Result.success(toVO(order));
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