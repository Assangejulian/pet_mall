package com.pat.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.controller.BaseController;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.service.IOrderItemService;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "订单明细", description = "订单商品明细 CRUD")
@RequestMapping("/api/order/item")
public class OrderItemController extends BaseController<OrderItem, OrderItem, OrderItem> {

    public OrderItemController(IOrderItemService service) {
        super(service);
    }

    @Override
    protected OrderItem toVO(OrderItem entity) {
        return entity;
    }

    @Override
    protected OrderItem toDO(OrderItem param) {
        return param;
    }

    @Override
    protected QueryWrapper<OrderItem> buildQueryWrapper(OrderItem param) {
        return new QueryWrapper<>();
    }
}