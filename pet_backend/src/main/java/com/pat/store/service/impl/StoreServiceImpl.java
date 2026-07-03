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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements IStoreService {

    private static final double EARTH_RADIUS_KM = 6371.0088;

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
    public boolean createStore(StoreDTO param, Long merchantUserId) {
        if (param == null || !StringUtils.hasText(param.getStoreName())) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "店铺名称不能为空");
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
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "店铺状态只能为0、1、2或3");
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
        if (storeId == null) throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "店铺ID不能为空");
        Store store = requireStore(storeId);
        if (store.getStatus() != 1) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "只有营业中店铺可以关店或删除");
        }
        Long onlineCount = countOnlineProducts(storeId);
        if (onlineCount != null && onlineCount > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "门店仍有上架商品，不能关店");
        }
    }

    public List<Store> getStoresByUserId(Long userId) {
        return lambdaQuery().eq(Store::getUserId, userId).eq(Store::getDeleted, 0).list();
    }

    @Override
    public List<Long> getStoreIdsByUserId(Long userId) {
        return getStoresByUserId(userId).stream().map(Store::getId).collect(Collectors.toList());
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

        List<NearbyStoreRow> allStores = baseMapper.selectAllActiveStores();
        if (allStores == null || allStores.isEmpty()) {
            Page<NearbyStoreRow> empty = new Page<>(current, size, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<NearbyStoreRow> filtered = allStores.stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .filter(s -> {
                    if (StringUtils.hasText(query.getKeyword())) {
                        return s.getStoreName() != null && s.getStoreName().contains(query.getKeyword());
                    }
                    return true;
                })
                .filter(s -> {
                    if (StringUtils.hasText(query.getCity())) {
                        return query.getCity().equals(s.getCity());
                    }
                    return true;
                })
                .map(s -> {
                    double dist = haversineKm(
                            query.getLongitude().doubleValue(),
                            query.getLatitude().doubleValue(),
                            s.getLongitude().doubleValue(),
                            s.getLatitude().doubleValue());
                    s.setDistanceKm(BigDecimal.valueOf(dist));
                    return s;
                })
                .filter(s -> s.getDistanceKm().compareTo(radiusKm) <= 0)
                .sorted(Comparator.comparingDouble(
                        s -> s.getDistanceKm() != null ? s.getDistanceKm().doubleValue() : Double.MAX_VALUE))
                .collect(Collectors.toList());

        long total = filtered.size();
        long from = (current - 1) * size;
        long to = Math.min(from + size, total);
        List<NearbyStoreRow> pageRows = (from < total) ? filtered.subList((int) from, (int) to) : Collections.emptyList();

        Page<NearbyStoreRow> result = new Page<>(current, size, total);
        result.setRecords(pageRows);
        return result;
    }

    private double haversineKm(double lng1, double lat1, double lng2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(Math.min(1.0, a)), Math.sqrt(Math.max(0.0, 1.0 - a)));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public List<Product> getStoreProducts(Long storeId) {
        return baseMapper.selectStoreProducts(storeId);
    }

    @Override
    public Store requireOwnedStore(Long storeId, Long merchantUserId) {
        if (storeId == null || merchantUserId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该商铺");
        }
        Store store = requireStore(storeId);
        if (!merchantUserId.equals(store.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该商铺");
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

    private Store transitionStatus(Long storeId, StoreStatusChange change) {
        return transitionStatus(requireStore(storeId), change);
    }

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
        return requireStore(storeId, ErrorCode.NOT_FOUND, "店铺不存在");
    }

    private Store requireStore(Long storeId, ErrorCode errorCode, String message) {
        if (storeId == null) throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "店铺ID不能为空");
        Store store = getById(storeId);
        if (store == null) throw new BusinessException(errorCode, message);
        return store;
    }
}
