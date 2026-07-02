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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@Tag(name = "门店管理（后台）", description = "管理端门店审核/CRUD")
@RequestMapping("/api/admin/store")
public class StoreController {

    private final IStoreService storeService;
    private final MapHelper mapHelper;

    public StoreController(IStoreService storeService, MapHelper mapHelper) {
        this.storeService = storeService;
        this.mapHelper = mapHelper;
    }

    @Operation(summary = "门店分页查询")
    @GetMapping("/search")
    public Result<IPage<StoreVO>> search(StoreDTO param, Page<Store> page) {
        String keyword = param == null ? null
                : (StringUtils.hasText(param.getKeyword()) ? param.getKeyword() : param.getStoreName());
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<Store>()
                .like(StringUtils.hasText(keyword), Store::getStoreName, keyword)
                .eq(param != null && param.getStatus() != null, Store::getStatus, param == null ? null : param.getStatus())
                .eq(param != null && StringUtils.hasText(param.getCity()), Store::getCity, param == null ? null : param.getCity())
                .orderByDesc(Store::getCreateTime);
        return Result.success(storeService.page(page, wrapper).convert(this::toVO));
    }

    @Operation(summary = "门店详情")
    @GetMapping("/{id}")
    public Result<StoreVO> detail(@PathVariable Long id) {
        Store store = storeService.getById(id);
        if (store == null) throw new BusinessException(ErrorCode.NOT_FOUND, "商店不存在");
        return Result.success(toVO(store));
    }

    @Operation(summary = "新增门店")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid StoreDTO param) {
        validateRequiredForCreate(param);
        fillCoordinates(param);
        storeService.validateStatus(param.getStatus());
        Store entity = new Store();
        BeanUtil.copyProperties(param, entity);
        if (entity.getStatus() == null) entity.setStatus(0);
        entity.setDeleted(0);
        if (!storeService.save(entity)) {
            throw new BusinessException(ErrorCode.SAVE_FAILED, "商店新增失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "修改门店")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid StoreDTO param) {
        fillCoordinates(param);
        storeService.validateStatus(param.getStatus());
        Store update = new Store();
        update.setId(id);
        copyEditableFields(param, update);
        if (!storeService.updateById(update)) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "商店修改失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "删除门店")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        storeService.ensureCanCloseOrDelete(id);
        return Result.success(storeService.removeById(id));
    }

    private StoreVO toVO(Store entity) {
        StoreVO vo = new StoreVO();
        BeanUtil.copyProperties(entity, vo);
        vo.setStatusText(statusText(entity.getStatus()));
        vo.setProductCount(storeService.countActiveProducts(entity.getId()));
        return vo;
    }

    private void validateRequiredForCreate(StoreDTO param) {
        if (param == null || !StringUtils.hasText(param.getStoreName())) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店名称不能为空");
        }
        if (param.getUserId() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "店主用户ID不能为空");
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

    private void copyEditableFields(StoreDTO param, Store target) {
        target.setStoreName(param.getStoreName());
        target.setStoreLogo(param.getStoreLogo());
        target.setStorePhone(param.getStorePhone());
        target.setStoreDesc(param.getStoreDesc());
        target.setProvince(param.getProvince());
        target.setCity(param.getCity());
        target.setDistrict(param.getDistrict());
        target.setAddress(param.getAddress());
        target.setLongitude(param.getLongitude());
        target.setLatitude(param.getLatitude());
    }

    private String statusText(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "营业中";
            case 2 -> "已关闭";
            case 3 -> "审核驳回";
            default -> String.valueOf(status);
        };
    }
}
