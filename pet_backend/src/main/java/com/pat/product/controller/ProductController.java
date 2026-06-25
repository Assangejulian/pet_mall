package com.pat.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.common.domain.Result;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.service.ProductService;
import com.pat.product.domain.vo.ProductVO;
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
@Tag(name = "商品公开接口", description = "小程序商品列表和详情")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "公开商品分页查询")
    @GetMapping({"/list", "/search"})
    public Result<IPage<ProductVO>> list(@Valid ProductQueryDTO query) {
        return Result.success(productService.pagePublicProducts(query));
    }

    @Operation(summary = "公开商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getPublicDetail(id));
    }
}
