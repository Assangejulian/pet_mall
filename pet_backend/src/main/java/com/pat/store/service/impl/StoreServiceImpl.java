package com.pat.store.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.entity.Store;
import com.pat.store.mapper.StoreMapper;
import com.pat.store.service.IStoreService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements IStoreService {

    @Override
    public void validateStatus(Integer status) {
        if (status == null) return;
        if (status < 0 || status > 2)
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商店状态只能为0、1或2");
    }

    @Override
    public void ensureCanCloseOrDelete(Long storeId) {
        if (storeId == null) throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店ID不能为空");
        Store store = getById(storeId);
        if (store == null) throw new BusinessException(ErrorCode.NOT_FOUND, "商店不存在");
        Long count = countActiveProducts(storeId);
        if (count != null && count > 0)
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商店仍有关联商品，不能关闭或删除");
    }

    @Override
    public Long countActiveProducts(Long storeId) {
        if (storeId == null) return 0L;
        return baseMapper.countActiveProducts(storeId);
    }

    @Override
    public Long countOnlineProducts(Long storeId) {
        if (storeId == null) return 0L;
        return baseMapper.countOnlineProducts(storeId);
    }

    @Override
    public List<Store> searchNearby(BigDecimal lat, BigDecimal lng, Double radius) {
        if (lat == null || lng == null) return List.of();
        if (radius == null || radius <= 0) radius = 5.0;
        return baseMapper.searchNearby(lat, lng, radius);
    }

    @Override
    public List<Product> getStoreProducts(Long storeId) {
        if (storeId == null) return List.of();
        return baseMapper.selectStoreProducts(storeId);
    }
}
