package com.pat.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.vo.CartVO;

import java.util.List;

public interface ICartService extends IService<Cart> {
    List<CartVO> getCurrentUserCart();
    void addToCart(Cart cart);
    void updateCartItem(Long id, Cart cart);
    void removeCartItem(Long id);
}
