package com.pat.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.order.domain.entity.Cart;

import java.util.List;

public interface ICartService extends IService<Cart> {
    List<Cart> getCurrentUserCart();
    void addToCart(Cart cart);
    void updateCartItem(Long id, Cart cart);
    void removeCartItem(Long id);
}
