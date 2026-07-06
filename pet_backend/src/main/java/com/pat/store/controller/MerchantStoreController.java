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
import com.pat.store.service.IStoreService;
import com.pat.store.helper.MapHelper;
import com.pat.common.util.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "门店管理（商家端）", description = "商家端门店 CRUD、营业状态管理")
@RestController
@RequestMapping("/api/merchant/store")
public class MerchantStoreController {

    private final IStoreService storeService;
    private final MapHelper mapHelper;

    public MerchantStoreController(IStoreService storeService, MapHelper mapHelper) {
        this.storeService = storeService;
        this.mapHelper = mapHelper;
    }

    @Operation(summary = "商家端门店分页查询")
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

    @Operation(summary = "门店详情（商家端）")
    @GetMapping("/{id}")
    public Result<StoreVO> detail(@PathVariable Long id) {
        return Result.success(toVO(storeService.requireOwnedStore(id, UserHolder.getUserId())));
    }

    @Operation(summary = "新增门店")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid StoreDTO param) {
        if (param == null || !StringUtils.hasText(param.getStoreName())) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店名称不能为空");
        }
        fillCoordinates(param);
        Store store = merchantStore(param);
        store.setUserId(UserHolder.getUserId());
        store.setStatus(0);
        store.setDeleted(0);
        if (!storeService.save(store)) throw new BusinessException(ErrorCode.SAVE_FAILED, "商店新增失败");
        return Result.success(true);
    }

    @Operation(summary = "修改门店")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid StoreDTO param) {
        if (!storeService.updateStore(id, param, UserHolder.getUserId())) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "商店修改失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "删除门店")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        storeService.requireOwnedStore(id, UserHolder.getUserId());
        storeService.ensureCanCloseOrDelete(id);
        if (!storeService.removeById(id)) {
            throw new BusinessException(ErrorCode.DELETE_FAILED, "商店删除失败");
        }
        return Result.success(true);
    }



    private StoreVO toVO(Store store) {
        StoreVO vo = new StoreVO();
        BeanUtil.copyProperties(store, vo);
        vo.setProductCount(storeService.countActiveProducts(store.getId()));
        vo.setStatusText(switch (store.getStatus() == null ? -1 : store.getStatus()) {
            case 0 -> "待审核";
            case 1 -> "营业中";
            case 2 -> "已关闭";
            case 3 -> "审核驳回";
            default -> String.valueOf(store.getStatus());
        });
        return vo;
    }

    private void fillCoordinates(StoreDTO param) {
        if (param.getLongitude() != null && param.getLatitude() != null) return;
        if (!StringUtils.hasText(param.getAddress())) return;
        BigDecimal[] coords = mapHelper.geocode(param.getProvince(), param.getCity(), param.getDistrict(), param.getAddress());
        if (coords != null) { param.setLongitude(coords[0]); param.setLatitude(coords[1]); }
    }

    private Store merchantStore(StoreDTO param) {
        Store store = new Store();
        store.setStoreName(param.getStoreName());
        store.setStoreLogo(param.getStoreLogo());
        store.setStorePhone(param.getStorePhone());
        store.setStoreDesc(param.getStoreDesc());
        store.setProvince(param.getProvince());
        store.setCity(param.getCity());
        store.setDistrict(param.getDistrict());
        store.setAddress(param.getAddress());
        store.setLongitude(param.getLongitude());
        store.setLatitude(param.getLatitude());
        return store;
    }

}
