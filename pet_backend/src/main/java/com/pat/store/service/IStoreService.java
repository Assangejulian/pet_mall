package com.pat.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.dto.NearbyQuery;
import com.pat.store.domain.entity.Store;
import com.pat.store.domain.vo.NearbyStoreRow;

import java.util.List;

public interface IStoreService extends IService<Store> {

    void validateStatus(Integer status);

    void ensureCanCloseOrDelete(Long storeId);

    Long countActiveProducts(Long storeId);

    Long countOnlineProducts(Long storeId);

    /** 附近门店搜索 */
    IPage<NearbyStoreRow> searchNearby(NearbyQuery query);

    /** 获取门店的商品列表 */
    List<Product> getStoreProducts(Long storeId);
}
