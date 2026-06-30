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
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auditor/store")
public class AuditorStoreController {

    private final IStoreService storeService;

    public AuditorStoreController(IStoreService storeService) {
        this.storeService = storeService;
    }

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

    @GetMapping("/{id}")
    public Result<StoreVO> detail(@PathVariable Long id) {
        Store store = storeService.getById(id);
        if (store == null) throw new BusinessException(ErrorCode.NOT_FOUND, "商店不存在");
        return Result.success(toVO(store));
    }

    @PutMapping("/{id}/approve")
    public Result<StoreVO> approve(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return audit(id, 1, "approve", reason);
    }

    @PutMapping("/{id}/reject")
    public Result<StoreVO> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return audit(id, 2, "reject", reason);
    }

    @PutMapping("/{id}/close")
    public Result<StoreVO> close(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return audit(id, 2, "close", reason);
    }

    private Result<StoreVO> audit(Long id, Integer status, String action, String reason) {
        Store store = storeService.updateAuditStatus(id, status);
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
            default -> String.valueOf(store.getStatus());
        });
        return vo;
    }
}
