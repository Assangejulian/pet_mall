package com.pat.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.product.entity.Product;
import com.pat.product.service.IProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController extends BaseController<Product, Product, Product> {

    public ProductController(IProductService service) {
        super(service);
    }

    @Override
    protected Product toVO(Product entity) {
        return entity;
    }

    @Override
    protected Product toDO(Product param) {
        return param;
    }

    @Override
    protected QueryWrapper<Product> buildQueryWrapper(Product param) {
        return new QueryWrapper<>();
    }
}