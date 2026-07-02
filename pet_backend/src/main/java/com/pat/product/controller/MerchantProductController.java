package com.pat.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.common.domain.Result;
import com.pat.product.domain.dto.ProductCreateDTO;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.domain.dto.ProductUpdateDTO;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.service.ProductService;
import com.pat.common.util.UserHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/product")
public class MerchantProductController {

    private final ProductService productService;

    public MerchantProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    public Result<IPage<ProductVO>> search(@Valid ProductQueryDTO query) {
        return Result.success(productService.pageMerchantProducts(query, UserHolder.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getMerchantDetail(id, UserHolder.getUserId()));
    }

    @PostMapping
    public Result<ProductVO> create(@RequestBody @Valid ProductCreateDTO dto) {
        return Result.success(productService.createMerchantProduct(dto, UserHolder.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @RequestBody @Valid ProductUpdateDTO dto) {
        return Result.success(productService.updateMerchantProduct(id, dto, UserHolder.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productService.deleteMerchantProduct(id, UserHolder.getUserId()));
    }

    @PutMapping("/{id}/online")
    public Result<ProductVO> online(@PathVariable Long id) {
        return Result.success(productService.onlineMerchantProduct(id, UserHolder.getUserId()));
    }

    @PutMapping("/{id}/offline")
    public Result<ProductVO> offline(@PathVariable Long id) {
        return Result.success(productService.offlineMerchantProduct(id, UserHolder.getUserId()));
    }
}
