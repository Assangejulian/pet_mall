package com.pat.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.order.entity.Cart;
import com.pat.order.service.ICartService;
import org.springframework.web.bind.annotation.*;

@RestController
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
        return new QueryWrapper<>();
    }
}