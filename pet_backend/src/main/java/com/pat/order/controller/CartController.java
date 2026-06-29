package com.pat.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.controller.BaseController;
import com.pat.order.domain.entity.Cart;
import com.pat.order.vo.CartVO;
import com.pat.order.service.ICartService;
import com.pat.product.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "购物车", description = "购物车 CRUD")
@RequestMapping("/api/cart")
public class CartController extends BaseController<Cart, Cart, CartVO> {

    private final ProductService productService;

    public CartController(ICartService service, ProductService productService) {
        super(service);
        this.productService = productService;
    }

    @Override
    protected void preSave(Cart param) {
        param.setUserId(com.pat.common.utils.UserHolder.getUserId());
    }

    @Override
    @PostMapping
    public com.pat.common.domain.Result<Boolean> save(@RequestBody @jakarta.validation.Valid Cart param) {
        param.setUserId(com.pat.common.utils.UserHolder.getUserId());
        Cart exist = baseService.getOne(new QueryWrapper<Cart>()
                .eq("user_id", param.getUserId())
                .eq("product_id", param.getProductId()));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + param.getQuantity());
            return com.pat.common.domain.Result.success(baseService.updateById(exist));
        }
        return super.save(param);
    }

    @Override
    protected CartVO toVO(Cart entity) {
        CartVO vo = new CartVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getProductId() != null) {
            vo.setProductInfo(productService.getById(entity.getProductId()));
        }
        return vo;
    }

    @Override
    protected Cart toDO(Cart param) {
        return param;
    }

    @Override
    protected QueryWrapper<Cart> buildQueryWrapper(Cart param) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", com.pat.common.utils.UserHolder.getUserId());
        return wrapper;
    }
}