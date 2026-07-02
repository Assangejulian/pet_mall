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
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "门店审核", description = "审核端门店审核/驳回")
@RestController
@RequestMapping("/api/auditor/store")
public class AuditorStoreController {

    private final IStoreService storeService;

    public AuditorStoreController(IStoreService storeService) {
        this.storeService = storeService;
    }

    @Operation(summary = "审核端门店分页查询")
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

    @Operation(summary = "门店详情（审核端）")
    @GetMapping("/{id}")
    public Result<StoreVO> detail(@PathVariable Long id) {
        Store store = storeService.getById(id);
        if (store == null) throw new BusinessException(ErrorCode.NOT_FOUND, "商店不存在");
        return Result.success(toVO(store));
    }

    @Operation(summary = "审核通过门店")
    @PutMapping("/{id}/approve")
    public Result<StoreVO> approve(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return audit(id, 1, "approve", normalizeOptional(reason));
    }

    @Operation(summary = "审核驳回门店")
    @PutMapping("/{id}/reject")
    public Result<StoreVO> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return audit(id, 3, "reject", requireReason(reason, "审核驳回原因不能为空"));
    }

    @Operation(summary = "关闭门店")
    @PutMapping("/{id}/close")
    public Result<StoreVO> close(@PathVariable Long id, @RequestParam(required = false) String reason) {
        String closeReason = requireReason(reason, "门店关闭原因不能为空");
        Store store = storeService.closeStore(id, closeReason);
        log.info("store audit action=close, storeId={}, reason={}", id, closeReason);
        return Result.success(toVO(store));
    }

    private Result<StoreVO> audit(Long id, Integer status, String action, String reason) {
        Store store = storeService.auditStore(id, status, UserHolder.getUserId(), reason);
        log.info("store audit action={}, storeId={}, reason={}", action, id, reason);
        return Result.success(toVO(store));
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

    private String requireReason(String reason, String message) {
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, message);
        }
        String value = reason.trim();
        if (value.length() > 500) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "原因长度不能超过500");
        }
        return value;
    }

    private String normalizeOptional(String reason) {
        if (!StringUtils.hasText(reason)) return null;
        String value = reason.trim();
        if (value.length() > 500) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "审核意见长度不能超过500");
        }
        return value;
    }
}
