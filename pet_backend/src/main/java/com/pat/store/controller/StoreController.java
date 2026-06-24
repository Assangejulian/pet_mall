package com.pat.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import com.pat.store.dto.StoreDTO;
import com.pat.store.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.store.vo.StoreVO;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/store")
public class StoreController extends BaseController<Store, StoreDTO, StoreVO> {

    private final IStoreService storeService;

    public StoreController(IStoreService service) {
        super(service);
        this.storeService = service;
    }

    @Override
    protected StoreVO toVO(Store entity) {
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
        vo.setStatusText(statusText(entity.getStatus()));
        vo.setProductCount(storeService.countActiveProducts(entity.getId()));
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    @Override
    protected Store toDO(StoreDTO param) {
        Store entity = new Store();
        entity.setId(param.getId());
        entity.setUserId(param.getUserId());
        entity.setStoreName(param.getStoreName());
        entity.setStoreLogo(param.getStoreLogo());
        entity.setStorePhone(param.getStorePhone());
        entity.setStoreDesc(param.getStoreDesc());
        entity.setProvince(param.getProvince());
        entity.setCity(param.getCity());
        entity.setDistrict(param.getDistrict());
        entity.setAddress(param.getAddress());
        entity.setLongitude(param.getLongitude());
        entity.setLatitude(param.getLatitude());
        entity.setStatus(param.getStatus());
        return entity;
    }

    @Override
    protected QueryWrapper<Store> buildQueryWrapper(StoreDTO param) {
        QueryWrapper<Store> wrapper = new QueryWrapper<>();
        if (param == null) {
            return wrapper.orderByDesc("create_time");
        }
        String keyword = StringUtils.hasText(param.getKeyword()) ? param.getKeyword() : param.getStoreName();
        wrapper.like(StringUtils.hasText(keyword), "store_name", keyword)
                .eq(param.getStatus() != null, "status", param.getStatus())
                .eq(StringUtils.hasText(param.getCity()), "city", param.getCity())
                .orderByDesc("create_time");
        return wrapper;
    }

    @Override
    protected void preSave(StoreDTO param) {
        validateRequiredForCreate(param);
        storeService.validateStatus(param.getStatus());
    }

    @Override
    protected void preUpdate(StoreDTO param) {
        storeService.validateStatus(param.getStatus());
    }

    @Override
    protected boolean doSave(Store entity, StoreDTO param) {
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        entity.setDeleted(0);
        return storeService.save(entity);
    }

    @Override
    protected boolean doUpdate(Long id, Store entity, StoreDTO param) {
        if (Integer.valueOf(2).equals(param.getStatus())) {
            storeService.ensureCanCloseOrDelete(id);
        }
        return storeService.updateById(entity);
    }

    @Override
    protected boolean doRemove(Long id) {
        storeService.ensureCanCloseOrDelete(id);
        return storeService.removeById(id);
    }

    @Override
    @GetMapping("/list")
    public Result<List<StoreVO>> getList(StoreDTO param) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商店列表请使用分页接口 /search");
    }

    @Override
    @PostMapping("/batch")
    public Result<Boolean> saveBatch(@RequestBody @Valid List<StoreDTO> paramList) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商店不支持批量新增");
    }

    @Override
    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody @Valid List<StoreDTO> paramList) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商店不支持批量修改");
    }

    @Override
    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商店不支持批量删除");
    }

    private void validateRequiredForCreate(StoreDTO param) {
        if (param == null || !StringUtils.hasText(param.getStoreName())) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店名称不能为空");
        }
        if (param.getUserId() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "店主用户ID不能为空");
        }
        if (param.getLongitude() == null || param.getLatitude() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "经纬度不能为空");
        }
    }

    private String statusText(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "营业中";
            case 2 -> "已关闭";
            default -> String.valueOf(status);
        };
    }
}
