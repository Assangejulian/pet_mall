package com.pat.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.product.entity.Product;
import com.pat.product.mapper.ProductMapper;
import com.pat.product.service.IProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {
}