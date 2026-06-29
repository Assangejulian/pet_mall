package com.pat.store.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.dto.NearbyQuery;
import com.pat.store.domain.entity.Store;
import com.pat.store.domain.vo.NearbyStoreRow;
import com.pat.store.mapper.StoreMapper;
import com.pat.store.service.IStoreService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
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
    public IPage<NearbyStoreRow> searchNearby(NearbyQuery query) {
        if (query == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "附近门店查询参数不能为空");
        }
        query.validateRequiredCoordinates();
        BigDecimal radiusKm = query.resolvedRadiusKm();
        long current = query.resolvedCurrent();
        long size = query.resolvedSize();
        Long totalValue = baseMapper.countNearby(query.getLongitude(), query.getLatitude(), radiusKm,
                query.getKeyword(), query.getCity());
        long total = totalValue == null ? 0L : totalValue;
        Page<NearbyStoreRow> result = new Page<>(current, size, total);
        long offset = calculateOffset(current, size, total);
        if (offset < 0 || total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }
        result.setRecords(baseMapper.selectNearby(query.getLongitude(), query.getLatitude(), radiusKm,
                query.getKeyword(), query.getCity(), size, offset));
        return result;
    }

    @Override
    public List<Product> getStoreProducts(Long storeId) {
        if (storeId == null) return List.of();
        return baseMapper.selectStoreProducts(storeId);
    }

    private long calculateOffset(long current, long size, long total) {
        if (current <= 1) {
            return 0L;
        }
        long pageIndex = current - 1;
        if (size <= 0 || pageIndex > total / size) {
            return -1L;
        }
        return pageIndex * size;
    }
}
