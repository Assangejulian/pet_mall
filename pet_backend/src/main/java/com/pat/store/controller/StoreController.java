package com.pat.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.domain.entity.Store;
import com.pat.store.helper.MapHelper;
import cn.hutool.core.bean.BeanUtil;
import com.pat.store.service.IStoreService;
import com.pat.store.domain.vo.StoreVO;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Tag(name = "门店管理（后台）", description = "管理端门店审核/CRUD")
@RequestMapping("/api/admin/store")
public class StoreController extends BaseController<Store, StoreDTO, StoreVO> {

    private final IStoreService storeService;
    private final MapHelper mapHelper;

    public StoreController(IStoreService service, MapHelper mapHelper) {
        super(service);
        this.storeService = service;
        this.mapHelper = mapHelper;
    }

    @Override
    protected StoreVO toVO(Store entity) {
        StoreVO vo = new StoreVO();
        BeanUtil.copyProperties(entity, vo);
        vo.setStatusText(statusText(entity.getStatus()));
        vo.setProductCount(storeService.countActiveProducts(entity.getId()));
        return vo;
    }

    @Override
    protected Store toDO(StoreDTO param) {
        Store entity = new Store();
        BeanUtil.copyProperties(param, entity);
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
        fillCoordinates(param);
        storeService.validateStatus(param.getStatus());
    }

    @Override
    protected void preUpdate(StoreDTO param) {
        fillCoordinates(param);
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
        Store update = new Store();
        update.setId(id);
        copyEditableFields(param, update);
        return storeService.updateById(update);
    }

    @Override
    protected boolean doRemove(Long id) {
        storeService.ensureCanCloseOrDelete(id);
        return storeService.removeById(id);
    }

    @Override
    @Operation(summary = "门店列表（禁用）")
    @GetMapping("/list")
    public Result<List<StoreVO>> getList(StoreDTO param) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商店列表请使用分页接口 /search");
    }

    @Override
    @Operation(summary = "门店批量新增（禁用）")
    @PostMapping("/batch")
    public Result<Boolean> saveBatch(@RequestBody @Valid List<StoreDTO> paramList) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商店不支持批量新增");
    }

    @Override
    @Operation(summary = "门店批量修改（禁用）")
    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody @Valid List<StoreDTO> paramList) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商店不支持批量修改");
    }

    @Override
    @Operation(summary = "门店批量删除（禁用）")
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
        if (!StringUtils.hasText(param.getAddress()) && (param.getLatitude() == null || param.getLongitude() == null)) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "地址或经纬度至少提供一个");
        }
    }

    /** 若未传坐标，通过高德地理编码自动补全 */
    private void fillCoordinates(StoreDTO param) {
        if (param.getLongitude() != null && param.getLatitude() != null) {
            return;
        }
        if (!StringUtils.hasText(param.getAddress())) {
            return;
        }
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
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "营业中";
            case 2 -> "已关闭";
            case 3 -> "审核驳回";
            default -> String.valueOf(status);
        };
    }
}
