package com.pat.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.common.domain.Result;
import com.pat.product.domain.dto.ProductCreateDTO;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.domain.dto.ProductUpdateDTO;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.service.ProductService;
import com.pat.store.service.IStoreService;
import com.pat.common.util.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品管理（商家端）", description = "商家端商品 CRUD、上下架")
@RestController
@RequestMapping("/api/merchant/product")
public class MerchantProductController {

    private final ProductService productService;
    private final IStoreService storeService;

    public MerchantProductController(ProductService productService,
                                     IStoreService storeService) {
        this.productService = productService;
        this.storeService = storeService;
    }

    @Operation(summary = "商家端商品分页查询")
    @GetMapping("/search")
    public Result<IPage<ProductVO>> search(@Valid ProductQueryDTO query) {
        return Result.success(productService.pageMerchantProducts(query, UserHolder.getUserId()));
    }

    @Operation(summary = "商品详情（商家端）")
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        productService.requireOwnedProduct(id, UserHolder.getUserId());
        return Result.success(productService.getAdminDetail(id));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<ProductVO> create(@RequestBody @Valid ProductCreateDTO dto) {
        storeService.requireOwnedStore(dto.getStoreId(), UserHolder.getUserId());
        return Result.success(productService.createProduct(dto));
    }

    @Operation(summary = "修改商品")
    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @RequestBody @Valid ProductUpdateDTO dto) {
        return Result.success(productService.updateMerchantProduct(id, dto, UserHolder.getUserId()));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        productService.requireOwnedProduct(id, UserHolder.getUserId());
        return Result.success(productService.deleteProduct(id));
    }

    @Operation(summary = "商品上架")
    @PutMapping("/{id}/online")
    public Result<ProductVO> online(@PathVariable Long id) {
        productService.requireOwnedProduct(id, UserHolder.getUserId());
        return Result.success(productService.onlineProduct(id));
    }

    @Operation(summary = "商品下架")
    @PutMapping("/{id}/offline")
    public Result<ProductVO> offline(@PathVariable Long id) {
        productService.requireOwnedProduct(id, UserHolder.getUserId());
        return Result.success(productService.offlineProduct(id));
    }
}
