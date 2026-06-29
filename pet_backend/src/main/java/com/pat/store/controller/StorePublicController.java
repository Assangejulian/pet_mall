package com.pat.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.dto.NearbyQuery;
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.store.domain.vo.StoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/store")
@Tag(name = "商店公开接口", description = "小程序营业中商店列表和详情")
public class StorePublicController {

    private final IStoreService storeService;

    public StorePublicController(IStoreService storeService) {
        this.storeService = storeService;
    }

    @Operation(summary = "公开商店分页查询")
    @GetMapping({"/search", "/list"})
    public Result<IPage<StoreVO>> search(@Valid StoreDTO param, Page<Store> page) {
        QueryWrapper<Store> wrapper = buildPublicWrapper(param).orderByDesc("create_time");
        Page<Store> result = storeService.page(page, wrapper);
        return Result.success(result.convert(this::toVO));
    }

    @Operation(summary = "公开商店详情")
    @GetMapping("/{id}")
    public Result<StoreVO> detail(@PathVariable Long id) {
        Store store = storeService.getOne(new QueryWrapper<Store>()
                .eq("id", id)
                .eq("status", 1), false);
        if (store == null) {
            return Result.error("商店不存在或未营业");
        }
        return Result.success(toVO(store));
    }

    @Operation(summary = "附近门店搜索 (Haversine)")
    @GetMapping("/nearby")
    public Result<List<StoreVO>> nearby(@Valid NearbyQuery query) {
        List<Store> list = storeService.searchNearby(query.getLatitude(), query.getLongitude(), query.getRadius());
        List<StoreVO> voList = list.stream().map(this::toVO).toList();
        return Result.success(voList);
    }

    @Operation(summary = "获取门店商品列表")
    @GetMapping("/{id}/products")
    public Result<List<Product>> storeProducts(@PathVariable Long id) {
        Store store = storeService.getById(id);
        if (store == null) {
            return Result.error("商店不存在");
        }
        return Result.success(storeService.getStoreProducts(id));
    }

    private QueryWrapper<Store> buildPublicWrapper(StoreDTO param) {
        QueryWrapper<Store> wrapper = new QueryWrapper<Store>().eq("status", 1);
        if (param == null) return wrapper;
        String keyword = StringUtils.hasText(param.getKeyword()) ? param.getKeyword() : param.getStoreName();
        wrapper.like(StringUtils.hasText(keyword), "store_name", keyword)
                .eq(StringUtils.hasText(param.getCity()), "city", param.getCity());
        return wrapper;
    }

    private StoreVO toVO(Store entity) {
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
        vo.setStatusText("营业中");
        vo.setProductCount(storeService.countActiveProducts(entity.getId()));
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
