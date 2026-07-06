package com.pat.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.common.util.UserHolder;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.vo.CartVO;
import com.pat.order.mapper.CartMapper;
import com.pat.order.service.ICartService;
import com.pat.product.domain.entity.Product;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.service.IProductService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现。
 *
 * <p>管理当前用户的购物车商品，支持增删改查和下单后批量清理。
 * 列表查询时批量关联商品信息，避免 N+1。</p>
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final IProductService productService;

    public CartServiceImpl(IProductService productService) {
        this.productService = productService;
    }

    /** 获取当前登录用户 ID。 */
    private Long requireUserId() {
        Long uid = UserHolder.getUserId();
        if (uid == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "用户未登录");
        }
        return uid;
    }

    /**
     * 校验购物车记录的归属权。
     *
     * @param id 购物车记录 ID
     * @return 归属当前用户的购物车记录
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

    /**
     * 获取当前用户的购物车列表。
     *
     * <p>批量查询关联商品信息，按创建时间倒序返回。</p>
     *
     * @return 含商品信息的购物车 VO 列表
     */
    @Override
    public List<CartVO> getCurrentUserCart() {
        List<Cart> carts = lambdaQuery()
                .eq(Cart::getUserId, requireUserId())
                .orderByDesc(Cart::getCreateTime)
                .list();
        if (carts.isEmpty()) return new ArrayList<>();

        List<Long> productIds = carts.stream().map(Cart::getProductId).collect(Collectors.toList());
        List<Product> products = productService.listByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

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

    /**
     * 添加商品到购物车。
     *
     * <p>如果该商品已在购物车中，则累加数量而非重复添加。</p>
     *
     * @param cart 购物车参数（productId、quantity）
     */
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
            if (cart.getChecked() != null) exist.setChecked(cart.getChecked());
            updateById(exist);
            return;
        }

        cart.setUserId(userId);
        cart.setQuantity(quantity);
        if (cart.getChecked() == null) cart.setChecked(1);
        save(cart);
    }

    /** 更新购物车项（数量、选中状态）。 */
    @Override
    public void updateCartItem(Long id, Cart cart) {
        Long userId = requireUserId();
        requireOwnedCart(id);
        cart.setId(id);
        cart.setUserId(userId);
        updateById(cart);
    }

    /** 删除购物车项。 */
    @Override
    public void removeCartItem(Long id) {
        requireOwnedCart(id);
        removeById(id);
    }

    /**
     * 下单后清理购物车中指定商品。
     *
     * @param userId     用户 ID
     * @param productIds 已下单的商品 ID 列表
     */
    @Override
    public void cleanByProductIds(Long userId, List<Long> productIds) {
        lambdaUpdate()
                .eq(Cart::getUserId, userId)
                .in(Cart::getProductId, productIds)
                .remove();
    }
}