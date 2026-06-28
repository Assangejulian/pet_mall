package com.pat.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.controller.BaseController;
import com.pat.order.domain.entity.Cart;
import com.pat.order.service.ICartService;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "购物车", description = "购物车 CRUD")
@RequestMapping("/api/cart")
public class CartController extends BaseController<Cart, Cart, Cart> {

    public CartController(ICartService service) {
        super(service);
    }

    @Override
    protected Cart toVO(Cart entity) {
        return entity;
    }

    @Override
    protected Cart toDO(Cart param) {
        return param;
    }

    @Override
    protected QueryWrapper<Cart> buildQueryWrapper(Cart param) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        if (param != null && param.getUserId() != null) {
            wrapper.eq("user_id", param.getUserId());
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}