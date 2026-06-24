package com.pat.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.common.domain.Result;
import com.pat.product.dto.ProductQueryDTO;
import com.pat.product.service.ProductService;
import com.pat.product.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/product")
@Tag(name = "商品公开接口", description = "商品公开查询")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "商品分页查询", description = "默认只返回上架且未删除商品，支持名称、商店、类型和分类筛选")
    @GetMapping("/list")
    public Result<IPage<ProductVO>> list(@Valid ProductQueryDTO query) {
        return Result.success(productService.pagePublicProducts(query));
    }

    @Operation(summary = "商品详情查询", description = "公开接口只返回上架且未删除商品")
    @GetMapping("/detail/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getPublicDetail(id));
    }

}
