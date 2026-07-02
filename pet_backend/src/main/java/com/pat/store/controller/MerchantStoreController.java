package com.pat.store.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.domain.entity.Store;
import com.pat.store.domain.vo.StoreVO;
import com.pat.store.helper.MapHelper;
import com.pat.store.service.IStoreService;
import com.pat.common.util.UserHolder;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/merchant/store")
public class MerchantStoreController {

    private final IStoreService storeService;
    private final MapHelper mapHelper;

    public MerchantStoreController(IStoreService storeService, MapHelper mapHelper) {
        this.storeService = storeService;
        this.mapHelper = mapHelper;
    }

    @GetMapping("/search")
    public Result<IPage<StoreVO>> search(StoreDTO param, Page<Store> page) {
        String keyword = param == null ? null
                : (StringUtils.hasText(param.getKeyword()) ? param.getKeyword() : param.getStoreName());
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<Store>()
                .eq(Store::getUserId, UserHolder.getUserId())
                .like(StringUtils.hasText(keyword), Store::getStoreName, keyword)
                .eq(param != null && param.getStatus() != null, Store::getStatus, param == null ? null : param.getStatus())
                .eq(param != null && StringUtils.hasText(param.getCity()), Store::getCity, param == null ? null : param.getCity())
                .orderByDesc(Store::getCreateTime);
        return Result.success(storeService.page(page, wrapper).convert(this::toVO));
    }

    @GetMapping("/{id}")
    public Result<StoreVO> detail(@PathVariable Long id) {
        return Result.success(toVO(storeService.requireOwnedStore(id, UserHolder.getUserId())));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid StoreDTO param) {
        validateCreate(param);
        fillCoordinates(param);
        Store store = new Store();
        BeanUtil.copyProperties(param, store);
        store.setUserId(UserHolder.getUserId());
        store.setStatus(0);
        store.setDeleted(0);
        if (!storeService.save(store)) {
            throw new BusinessException(ErrorCode.SAVE_FAILED, "商店新增失败");
        }
        return Result.success(true);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid StoreDTO param) {
        Store original = storeService.requireOwnedStore(id, UserHolder.getUserId());
        fillCoordinates(param);
        Store update = new Store();
        BeanUtil.copyProperties(param, update);
        update.setId(id);
        update.setUserId(original.getUserId());
        update.setStatus(0);
        update.setDeleted(null);
        if (!storeService.updateById(update)) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "商店修改失败");
        }
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        storeService.requireOwnedStore(id, UserHolder.getUserId());
        storeService.ensureCanCloseOrDelete(id);
        if (!storeService.removeById(id)) {
            throw new BusinessException(ErrorCode.DELETE_FAILED, "商店删除失败");
        }
        return Result.success(true);
    }

    private void validateCreate(StoreDTO param) {
        if (param == null || !StringUtils.hasText(param.getStoreName())) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店名称不能为空");
        }
        if (!StringUtils.hasText(param.getAddress()) && (param.getLatitude() == null || param.getLongitude() == null)) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "地址或经纬度至少提供一个");
        }
    }

    private void fillCoordinates(StoreDTO param) {
        if (param.getLongitude() != null && param.getLatitude() != null) return;
        if (!StringUtils.hasText(param.getAddress())) return;
        BigDecimal[] coords = mapHelper.geocode(param.getProvince(), param.getCity(), param.getDistrict(), param.getAddress());
        if (coords != null) {
            param.setLongitude(coords[0]);
            param.setLatitude(coords[1]);
        }
    }

    private StoreVO toVO(Store store) {
        StoreVO vo = new StoreVO();
        BeanUtil.copyProperties(store, vo);
        vo.setProductCount(storeService.countActiveProducts(store.getId()));
        vo.setStatusText(switch (store.getStatus() == null ? -1 : store.getStatus()) {
            case 0 -> "待审核";
            case 1 -> "营业中";
            case 2 -> "已关闭";
            default -> String.valueOf(store.getStatus());
        });
        return vo;
    }
}
