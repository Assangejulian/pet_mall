package com.pat.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.vo.CartVO;
import com.pat.order.mapper.CartMapper;
import com.pat.order.service.ICartService;
import com.pat.product.domain.entity.Product;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.service.IProductService;
import com.pat.common.util.UserHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final IProductService productService;

    public CartServiceImpl(IProductService productService) {
        this.productService = productService;
    }

    private Long requireUserId() {
        Long uid = UserHolder.getUserId();
        if (uid == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "用户未登录");
        }
        return uid;
    }

    /**
     * 校验购物车记录归属当前用户，防止越权操作。
     *
     * @param id 购物车记录 ID
     * @return 归属当前用户的购物车记录
     * @throws BusinessException 记录不存在或无权限
     */
    private Cart requireOwnedCart(Long id) {
        Long userId = requireUserId();
        Cart cart = getById(id);
        if (cart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车记录不存在");
        }
        if (!userId.equals(cart.getUserId())) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "无权操作");
        }
        return cart;
    }

    @Override
    public List<CartVO> getCurrentUserCart() {
        List<Cart> carts = lambdaQuery()
                .eq(Cart::getUserId, requireUserId())
                .orderByDesc(Cart::getCreateTime)
                .list();

        if (carts.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询商品信息
        List<Long> productIds = new ArrayList<>(carts.size());
        for (Cart cart : carts) {
            productIds.add(cart.getProductId());
        }
        List<Product> products = productService.listByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        // 组装 CartVO
        List<CartVO> result = new ArrayList<>(carts.size());
        for (Cart cart : carts) {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());
            vo.setChecked(cart.getChecked());

            Product product = productMap.get(cart.getProductId());
            if (product != null) {
                ProductVO productVO = new ProductVO();
                productVO.setId(product.getId());
                productVO.setName(product.getProductName());
                productVO.setPrice(product.getPrice());
                productVO.setImage(product.getMainImage());
                productVO.setMainImage(product.getMainImage());
                productVO.setStock(product.getStock());
                productVO.setDetail(product.getProductDesc());
                vo.setProductInfo(productVO);
            }

            result.add(vo);
        }
        return result;
    }

    @Override
    public void addToCart(Cart cart) {
        Long userId = requireUserId();
        if (cart.getProductId() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "productId不能为空");
        }

        int quantity = cart.getQuantity() == null || cart.getQuantity() < 1 ? 1 : cart.getQuantity();
        Cart exist = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, cart.getProductId())
                .one();
        if (exist != null) {
            exist.setQuantity((exist.getQuantity() == null ? 0 : exist.getQuantity()) + quantity);
            if (cart.getChecked() != null) {
                exist.setChecked(cart.getChecked());
            }
            updateById(exist);
            return;
        }

        cart.setUserId(userId);
        cart.setQuantity(quantity);
        if (cart.getChecked() == null) {
            cart.setChecked(1);
        }
        save(cart);
    }

    @Override
    public void updateCartItem(Long id, Cart cart) {
        Long userId = requireUserId();
        requireOwnedCart(id);
        cart.setId(id);
        cart.setUserId(userId);
        updateById(cart);
    }

    @Override
    public void removeCartItem(Long id) {
        requireOwnedCart(id);
        removeById(id);
    }
}