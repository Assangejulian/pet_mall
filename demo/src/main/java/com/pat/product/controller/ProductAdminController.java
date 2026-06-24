package com.pat.product.controller;

import com.pat.common.domain.Result;
import com.pat.product.dto.ProductCreateDTO;
import com.pat.product.dto.ProductQueryDTO;
import com.pat.product.dto.ProductUpdateDTO;
import com.pat.product.service.ProductService;
import com.pat.product.vo.ProductPageVO;
import com.pat.product.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/product")
@Tag(name = "商品管理接口", description = "管理端商品新增、修改、上下架、删除和分页")
public class ProductAdminController {

    private final ProductService productService;

    public ProductAdminController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "管理端商品分页查询")
    @GetMapping("/list")
    public Result<ProductPageVO> list(@Valid ProductQueryDTO query) {
        return Result.success(productService.pageAdminProducts(query));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<ProductVO> create(@RequestBody @Valid ProductCreateDTO dto) {
        return Result.success(productService.createProduct(dto));
    }

    @Operation(summary = "修改商品")
    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @RequestBody @Valid ProductUpdateDTO dto) {
        return Result.success(productService.updateProduct(id, dto));
    }

    @Operation(summary = "逻辑删除商品")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productService.deleteProduct(id));
    }

    @Operation(summary = "商品上架")
    @PutMapping("/{id}/online")
    public Result<ProductVO> online(@PathVariable Long id) {
        return Result.success(productService.onlineProduct(id));
    }

    @Operation(summary = "商品下架")
    @PutMapping("/{id}/offline")
    public Result<ProductVO> offline(@PathVariable Long id) {
        return Result.success(productService.offlineProduct(id));
    }
}
