package com.pat.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.store.domain.vo.StoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/store")
@Tag(name = "商店公开接口", description = "小程序营业中商店列表和详情")
public class StorePublicController {

    private static final BigDecimal DEFAULT_RADIUS_KM = BigDecimal.TEN;
    private static final BigDecimal MAX_RADIUS_KM = BigDecimal.valueOf(100);
    private static final long MAX_PAGE_SIZE = 100L;
    private static final double EARTH_RADIUS_KM = 6371.0088D;

    private final IStoreService storeService;

    public StorePublicController(IStoreService storeService) {
        this.storeService = storeService;
    }

    @Operation(summary = "公开商店分页查询")
    @GetMapping({"/search", "/list"})
    public Result<IPage<StoreVO>> search(@Valid StoreDTO param, Page<Store> page) {
        QueryWrapper<Store> wrapper = buildPublicWrapper(param).orderByDesc("create_time");
        Page<Store> result = storeService.page(page, wrapper);
        return Result.success(result.convert(this::toVO));
    }

    @Operation(summary = "公开商店详情")
    @GetMapping("/{id}")
    public Result<StoreVO> detail(@PathVariable Long id) {
        Store store = storeService.getOne(new QueryWrapper<Store>()
                .eq("id", id)
                .eq("status", 1), false);
        if (store == null) {
            return Result.error("商店不存在或未营业");
        }
        return Result.success(toVO(store));
    }

    @Operation(summary = "附近商店分页查询")
    @GetMapping("/nearby")
    public Result<IPage<StoreVO>> nearby(@Valid StoreDTO param, Page<Store> page) {
        validateNearbyParam(param);
        BigDecimal radiusKm = resolveRadius(param);
        List<NearbyStore> matched = storeService.list(buildPublicWrapper(param)).stream()
                .filter(store -> store.getLongitude() != null && store.getLatitude() != null)
                .map(store -> new NearbyStore(store,
                        calculateRawDistanceKm(param.getLongitude(), param.getLatitude(),
                                store.getLongitude(), store.getLatitude())))
                .filter(item -> BigDecimal.valueOf(item.distanceKm()).compareTo(radiusKm) <= 0)
                .sorted(Comparator.comparingDouble(NearbyStore::distanceKm)
                        .thenComparing(item -> item.store().getId()))
                .toList();

        long current = normalizeCurrent(page.getCurrent());
        long size = normalizeSize(page.getSize());
        long from = calculateOffset(current, size, matched.size());
        long to = Math.min(from + size, matched.size());
        Page<StoreVO> result = new Page<>(current, size, matched.size());
        result.setRecords(matched.subList((int) from, (int) to).stream()
                .map(item -> toNearbyVO(item.store(), item.distanceKm()))
                .toList());
        return Result.success(result);
    }

    private QueryWrapper<Store> buildPublicWrapper(StoreDTO param) {
        QueryWrapper<Store> wrapper = new QueryWrapper<Store>().eq("status", 1);
        if (param == null) {
            return wrapper;
        }
        String keyword = StringUtils.hasText(param.getKeyword()) ? param.getKeyword() : param.getStoreName();
        wrapper.like(StringUtils.hasText(keyword), "store_name", keyword)
                .eq(StringUtils.hasText(param.getCity()), "city", param.getCity());
        return wrapper;
    }

    private void validateNearbyParam(StoreDTO param) {
        if (param == null || param.getLongitude() == null || param.getLatitude() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "经纬度不能为空");
        }
        if (param.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0
                || param.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "经度范围必须在-180到180之间");
        }
        if (param.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0
                || param.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "纬度范围必须在-90到90之间");
        }
        resolveRadius(param);
    }

    private BigDecimal resolveRadius(StoreDTO param) {
        BigDecimal radiusKm = param.getRadiusKm() == null ? DEFAULT_RADIUS_KM : param.getRadiusKm();
        if (radiusKm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "查询半径必须大于0");
        }
        if (radiusKm.compareTo(MAX_RADIUS_KM) > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "查询半径不能超过100公里");
        }
        return radiusKm;
    }

    private long normalizeCurrent(long current) {
        return current <= 0 ? 1L : current;
    }

    private long normalizeSize(long size) {
        if (size <= 0) {
            return 10L;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private long calculateOffset(long current, long size, long total) {
        if (current <= 1) {
            return 0L;
        }
        long pageIndex = current - 1;
        if (pageIndex > total / size) {
            return total;
        }
        return Math.min(pageIndex * size, total);
    }

    private StoreVO toNearbyVO(Store entity, double distanceKm) {
        StoreVO vo = toVO(entity);
        vo.setDistanceKm(BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP));
        return vo;
    }

    private double calculateRawDistanceKm(BigDecimal fromLongitude, BigDecimal fromLatitude,
                                          BigDecimal toLongitude, BigDecimal toLatitude) {
        double lon1 = Math.toRadians(fromLongitude.doubleValue());
        double lat1 = Math.toRadians(fromLatitude.doubleValue());
        double lon2 = Math.toRadians(toLongitude.doubleValue());
        double lat2 = Math.toRadians(toLatitude.doubleValue());
        double deltaLon = lon2 - lon1;
        double deltaLat = lat2 - lat1;
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
        a = Math.max(0D, Math.min(1D, a));
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    private record NearbyStore(Store store, double distanceKm) {
    }

    private StoreVO toVO(Store entity) {
        StoreVO vo = new StoreVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setStoreName(entity.getStoreName());
        vo.setStoreLogo(entity.getStoreLogo());
        vo.setStorePhone(entity.getStorePhone());
        vo.setStoreDesc(entity.getStoreDesc());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setAddress(entity.getAddress());
        vo.setLongitude(entity.getLongitude());
        vo.setLatitude(entity.getLatitude());
        vo.setStatus(entity.getStatus());
        vo.setStatusText("营业中");
        vo.setProductCount(storeService.countActiveProducts(entity.getId()));
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
