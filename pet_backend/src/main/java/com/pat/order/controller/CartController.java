package com.pat.order.controller;

import com.pat.common.domain.Result;
import com.pat.order.domain.entity.Cart;
import com.pat.order.service.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车", description = "当前用户的购物车")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "当前用户购物车列表")
    @GetMapping({"/search", "/list"})
    public Result<List<Cart>> list() {
        return Result.success(cartService.getCurrentUserCart());
    }

    @Operation(summary = "添加到购物车")
    @PostMapping
    public Result<Void> add(@RequestBody Cart cart) {
        cartService.addToCart(cart);
        return Result.success();
    }

    @Operation(summary = "修改购物车")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Cart cart) {
        cartService.updateCartItem(id, cart);
        return Result.success();
    }

    @Operation(summary = "删除购物车")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.removeCartItem(id);
        return Result.success();
    }
}
