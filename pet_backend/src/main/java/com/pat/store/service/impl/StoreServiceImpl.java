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
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.helper.MapHelper;
import com.pat.store.service.IStoreService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements IStoreService {

    private record StoreStatusChange(
            Integer targetStatus,
            Long auditUserId,
            String auditRemark,
            String closeReason,
            String failMsg,
            boolean checkDeleted) {
    }

    private final MapHelper mapHelper;

    public StoreServiceImpl(MapHelper mapHelper) {
        this.mapHelper = mapHelper;
    }


    @Override
    /**
     * 商户/管理员创建门店。merchantUserId 非空表示走商家端流程（userId 用当前登录用户，状态置为待审核）。
     */
    public boolean createStore(StoreDTO param, Long merchantUserId) {
        if (param == null || !StringUtils.hasText(param.getStoreName())) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店名称不能为空");
        }
        fillCoordinates(param);
        Store entity = Store.from(param);
        if (merchantUserId != null) {
            entity.setUserId(merchantUserId);
            entity.setStatus(0);
        } else {
            if (param.getUserId() == null) {
                throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "店主用户ID不能为空");
            }
            entity.setUserId(param.getUserId());
            if (entity.getStatus() == null) entity.setStatus(0);
        }
        entity.setDeleted(0);
        return save(entity);
    }

    @Override
    /**
     * 商户/管理员修改门店。商户端修改后状态重置为待审核。
     */
    public boolean updateStore(Long id, StoreDTO param, Long merchantUserId) {
        fillCoordinates(param);
        if (merchantUserId != null) {
            requireOwnedStore(id, merchantUserId);
        }
        Store entity = Store.from(param);
        entity.setId(id);
        entity.setDeleted(null);
        return updateById(entity);
    }

    /**
     * 地址反查经纬度。如果 DTO 已有经纬度则跳过，避免覆盖手工填写的精确坐标。
     */
    private void fillCoordinates(StoreDTO param) {
        if (param.getLongitude() != null && param.getLatitude() != null) return;
        if (!StringUtils.hasText(param.getAddress())) return;
        java.math.BigDecimal[] coords = mapHelper.geocode(
                param.getProvince(), param.getCity(), param.getDistrict(), param.getAddress());
        if (coords != null) {
            param.setLongitude(coords[0]);
            param.setLatitude(coords[1]);
        }
    }

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
        var change = new StoreStatusChange(1, auditUserId, null, null, "店铺重新开业失败", false);
        return transitionStatus(storeId, change);
    }

    @Override
    public Store resubmitStore(Long storeId) {
        var change = new StoreStatusChange(0, null, null, null, "店铺重新提交审核失败", false);
        return transitionStatus(storeId, change);
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
        List<NearbyStoreRow> rows = baseMapper.selectNearby(query.getLongitude(), query.getLatitude(),
                radiusKm, query.getKeyword(), query.getCity(), offset, size);
        result.setRecords(rows);
        return result;
    }

    @Override
    public List<Product> getStoreProducts(Long storeId) {
        if (storeId == null) return List.of();
        return baseMapper.selectStoreProducts(storeId);
    }

    @Override
    public List<Long> getStoreIdsByUserId(Long userId) {
        return lambdaQuery()
                .eq(Store::getUserId, userId)
                .list()
                .stream()
                .map(Store::getId)
                .collect(Collectors.toList());
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
        var change = new StoreStatusChange(status, auditUserId, auditRemark, null, "只有待审核门店可以执行审核", true);
        return transitionStatus(store, change);
    }

    @Override
    public Store closeStore(Long storeId, String closeReason) {
        if (!StringUtils.hasText(closeReason)) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "门店关闭原因不能为空");
        }
        Store store = requireStore(storeId);
        if (!Integer.valueOf(1).equals(store.getStatus())) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "只有营业中门店可以关闭");
        }
        Long onlineCount = countOnlineProducts(storeId);
        if (onlineCount != null && onlineCount > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "门店仍有上架商品，不能关闭");
        }
        var change = new StoreStatusChange(2, null, null, closeReason, "只有营业中门店可以关闭", true);
        return transitionStatus(store, change);
    }
    /**
     * 统一的状态变更方法（按 storeId 查）+ 校验状态机 → 乐观锁更新 → 回写实体
     */
    private Store transitionStatus(Long storeId, StoreStatusChange change) {
        return transitionStatus(requireStore(storeId), change);
    }

    /**
     * 统一的状态变更方法：校验状态机 → 乐观锁更新 → 回写实体
     */
    private Store transitionStatus(Store store, StoreStatusChange change) {
        StoreStateMachine.validate(store.getStatus(), change.targetStatus());
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<Store> wrapper = new LambdaUpdateWrapper<Store>()
                .set(Store::getStatus, change.targetStatus())
                .set(change.auditUserId() != null, Store::getAuditUserId, change.auditUserId())
                .set(Store::getAuditTime, now)
                .set(Store::getAuditRemark, change.auditRemark())
                .set(Store::getCloseReason, change.closeReason())
                .eq(Store::getId, store.getId())
                .eq(Store::getStatus, store.getStatus());

        if (change.checkDeleted()) {
            wrapper.eq(Store::getDeleted, 0);
        }

        int rows = baseMapper.update(null, wrapper);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, change.failMsg());
        }

        store.setStatus(change.targetStatus());
        if (change.auditUserId() != null) store.setAuditUserId(change.auditUserId());
        store.setAuditTime(now);
        store.setAuditRemark(change.auditRemark());
        store.setCloseReason(change.closeReason());
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
