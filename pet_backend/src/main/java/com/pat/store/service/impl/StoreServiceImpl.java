package com.pat.store.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.dto.NearbyQuery;
import com.pat.store.domain.entity.Store;
import com.pat.store.helper.StoreStateMachine;
import com.pat.store.domain.vo.NearbyStoreRow;
import com.pat.store.mapper.StoreMapper;
import com.pat.store.service.IStoreService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements IStoreService {

    @Override
    public void validateStatus(Integer status) {
        if (status == null) return;
        if (status < 0 || status > 3)
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商店状态只能为0、1、2或3");
    }

    @Override
    public Store reopenStore(Long storeId, Long auditUserId) {
        if (auditUserId == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "未获取到当前审核人员");
        }
        Store store = requireStore(storeId);
        StoreStateMachine.validate(store.getStatus(), 1);
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<Store>()
                .set(Store::getStatus, 1)
                .set(Store::getAuditUserId, auditUserId)
                .set(Store::getAuditTime, LocalDateTime.now())
                .set(Store::getAuditRemark, (String) null)
                .set(Store::getCloseReason, (String) null)
                .eq(Store::getId, storeId)
                .eq(Store::getStatus, store.getStatus()));
        if (rows != 1) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "店铺重新开业失败");
        }
        store.setStatus(1);
        store.setAuditUserId(auditUserId);
        store.setAuditTime(LocalDateTime.now());
        store.setAuditRemark(null);
        store.setCloseReason(null);
        return store;
    }

    @Override
    public Store resubmitStore(Long storeId) {
        Store store = requireStore(storeId);
        StoreStateMachine.validate(store.getStatus(), 0);
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<Store>()
                .set(Store::getStatus, 0)
                .set(Store::getAuditRemark, (String) null)
                .eq(Store::getId, storeId)
                .eq(Store::getStatus, store.getStatus()));
        if (rows != 1) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "店铺重新提交审核失败");
        }
        store.setStatus(0);
        store.setAuditRemark(null);
        return store;
    }

    @Override
    public void ensureCanCloseOrDelete(Long storeId) {
        if (storeId == null) throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店ID不能为空");
        Store store = requireStore(storeId);
        if (Integer.valueOf(0).equals(store.getStatus())) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "待审核门店不能删除");
        }
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
        query.validate();
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

    @Override
    public Store requireOwnedStore(Long storeId, Long merchantUserId) {
        if (storeId == null || merchantUserId == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店ID和商家用户ID不能为空");
        }
        Store store = requireStore(storeId);
        if (!merchantUserId.equals(store.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该商店");
        }
        return store;
    }

    @Override
    public boolean isOwnedStore(Long storeId, Long merchantUserId) {
        if (storeId == null || merchantUserId == null) {
            return false;
        }
        Store store = getById(storeId);
        return store != null && merchantUserId.equals(store.getUserId());
    }

    @Override
    public Store auditStore(Long storeId, Integer status, Long auditUserId, String auditRemark) {
        if (auditUserId == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "未获取到当前审核人员");
        }
        if (status == 3 && !StringUtils.hasText(auditRemark)) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "审核驳回原因不能为空");
        }
        Store store = requireStore(storeId);
        if (!Integer.valueOf(0).equals(store.getStatus())) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "只有待审核门店可以执行审核");
        }
        StoreStateMachine.validate(store.getStatus(), status);
        LocalDateTime auditTime = LocalDateTime.now();
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<Store>()
                .set(Store::getStatus, status)
                .set(Store::getAuditUserId, auditUserId)
                .set(Store::getAuditTime, auditTime)
                .set(Store::getAuditRemark, auditRemark)
                .eq(Store::getId, storeId)
                .eq(Store::getStatus, store.getStatus())
                .eq(Store::getDeleted, 0));
        if (rows != 1) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "只有待审核门店可以执行审核");
        }
        store.setStatus(status);
        store.setAuditUserId(auditUserId);
        store.setAuditTime(auditTime);
        store.setAuditRemark(auditRemark);
        return store;
    }

    @Override
    public Store closeStore(Long storeId, String closeReason) {
        if (!StringUtils.hasText(closeReason)) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "门店关闭原因不能为空");
        }
        Store store = getById(storeId);
        if (store == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商店不存在");
        }
        if (!Integer.valueOf(1).equals(store.getStatus())) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "只有营业中门店可以关闭");
        }
        StoreStateMachine.validate(store.getStatus(), 2);
        Long onlineCount = countOnlineProducts(storeId);
        if (onlineCount != null && onlineCount > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "门店仍有上架商品，不能关闭");
        }
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<Store>()
                .set(Store::getStatus, 2)
                .set(Store::getCloseReason, closeReason)
                .eq(Store::getId, storeId)
                .eq(Store::getStatus, store.getStatus())
                .eq(Store::getDeleted, 0));
        if (rows != 1) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "只有营业中门店可以关闭");
        }
        store.setStatus(2);
        store.setCloseReason(closeReason);
        return store;
    }

    private Store requireStore(Long storeId) {
        return requireStore(storeId, ErrorCode.NOT_FOUND, "商店不存在");
    }

    private Store requireStore(Long storeId, ErrorCode errorCode, String message) {
        if (storeId == null) throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店ID不能为空");
        Store store = getById(storeId);
        if (store == null) throw new BusinessException(errorCode, message);
        return store;
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
