package com.pat.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.store.entity.Store;

public interface IStoreService extends IService<Store> {

    void validateStatus(Integer status);

    void ensureCanCloseOrDelete(Long storeId);

    Long countActiveProducts(Long storeId);

    Long countOnlineProducts(Long storeId);
}
