package com.pat.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.product.domain.entity.Product;

public interface IProductService extends IService<Product> {

    /**
     * 行级锁查询 — 用于下单扣库存时防超卖
     */
    Product getForUpdate(Long id);
}