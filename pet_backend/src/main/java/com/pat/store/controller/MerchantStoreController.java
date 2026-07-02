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

    public MerchantStoreController(IStoreService storeService) {
        this.storeService = storeService;
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
        return Result.success(storeService.createStore(param, UserHolder.getUserId()));
    }

    @Operation(summary = "修改门店")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid StoreDTO param) {
        return Result.success(storeService.updateStore(id, param, UserHolder.getUserId()));
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

}
