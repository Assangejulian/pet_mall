package com.pat.store.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.common.util.StatusDisplayUtil;
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
        if (param == null || param.getUserId() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "店主用户ID不能为空");
        }
        fillCoordinates(param);
        Store store = Store.from(param);
        store.setUserId(param.getUserId());
        // 管理端创建也必须从待审核状态开始，任何请求体中的流程字段都不能生效。
        store.setStatus(0);
        store.setAuditUserId(null);
        store.setAuditTime(null);
        store.setAuditRemark(null);
        store.setCloseReason(null);
        store.setDeleted(0);
        return Result.success(storeService.save(store));
    }

    @Operation(summary = "修改门店")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid StoreDTO param) {
        fillCoordinates(param);
        Store update = Store.from(param);
        update.setId(id);
        update.setUserId(null);
        update.setStatus(null);
        update.setAuditUserId(null);
        update.setAuditTime(null);
        update.setAuditRemark(null);
        update.setCloseReason(null);
        update.setDeleted(null);
        return Result.success(storeService.updateById(update));
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
        vo.setStatusText(StatusDisplayUtil.storeStatus(entity.getStatus()));
        vo.setProductCount(storeService.countActiveProducts(entity.getId()));
        return vo;
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




}
