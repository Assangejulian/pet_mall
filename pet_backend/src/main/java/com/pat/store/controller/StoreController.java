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

    public StoreController(IStoreService storeService) {
        this.storeService = storeService;
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
        return Result.success(storeService.createStore(param, null));
    }

    @Operation(summary = "修改门店")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid StoreDTO param) {
        return Result.success(storeService.updateStore(id, param, null));
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
