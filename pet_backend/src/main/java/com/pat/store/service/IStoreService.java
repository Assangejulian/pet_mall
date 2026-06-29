package com.pat.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.entity.Store;

import java.math.BigDecimal;
import java.util.List;

public interface IStoreService extends IService<Store> {

    void validateStatus(Integer status);

    void ensureCanCloseOrDelete(Long storeId);

    Long countActiveProducts(Long storeId);

    Long countOnlineProducts(Long storeId);

    /** 附近门店搜索 */
    List<Store> searchNearby(BigDecimal lat, BigDecimal lng, Double radius);

    /** 获取门店的商品列表 */
    List<Product> getStoreProducts(Long storeId);
}
