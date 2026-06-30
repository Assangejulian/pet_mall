package com.pat.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.entity.Cart;
import com.pat.order.mapper.CartMapper;
import com.pat.order.service.ICartService;
import com.pat.user.utils.UserHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private Long requireUserId() {
        Long uid = UserHolder.getUserId();
        if (uid == null) throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "用户未登录");
        return uid;
    }

    @Override
    public List<Cart> getCurrentUserCart() {
        return lambdaQuery()
                .eq(Cart::getUserId, requireUserId())
                .orderByDesc(Cart::getCreateTime)
                .list();
    }

    @Override
    public void addToCart(Cart cart) {
        cart.setUserId(requireUserId());
        save(cart);
    }

    @Override
    public void updateCartItem(Long id, Cart cart) {
        Long userId = requireUserId();
        Cart exist = getById(id);
        if (exist == null)
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车记录不存在");
        if (!userId.equals(exist.getUserId()))
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "无权操作");
        cart.setId(id);
        cart.setUserId(userId);
        updateById(cart);
    }

    @Override
    public void removeCartItem(Long id) {
        Long userId = requireUserId();
        Cart exist = getById(id);
        if (exist == null)
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车记录不存在");
        if (!userId.equals(exist.getUserId()))
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "无权操作");
        removeById(id);
    }
}
