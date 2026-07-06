package com.pat.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.vo.CartVO;

import java.util.List;

/**
 * 购物车操作接口。
 *
 * <p>定义当前用户的购物车 CRUD 操作，以及下单后清理购物车的能力。
 * 实现类 {@link com.pat.order.service.impl.CartServiceImpl}。</p>
 */
public interface ICartService extends IService<Cart> {

    /** 获取当前用户的购物车列表（含商品信息）。 */
    List<CartVO> getCurrentUserCart();

    /** 添加商品到购物车（已有则累加数量）。 */
    void addToCart(Cart cart);

    /** 更新购物车项（数量、选中状态）。 */
    void updateCartItem(Long id, Cart cart);

    /** 删除购物车项。 */
    void removeCartItem(Long id);

    /**
     * 下单后清除购物车中指定商品。
     *
     * @param userId     用户 ID
     * @param productIds 已下单的商品 ID 列表
     */
    void cleanByProductIds(Long userId, List<Long> productIds);
}