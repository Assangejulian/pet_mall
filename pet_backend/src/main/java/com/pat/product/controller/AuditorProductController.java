package com.pat.product.controller;

import com.pat.common.domain.Result;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.domain.vo.ProductPageVO;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auditor/product")
public class AuditorProductController {

    private final ProductService productService;

    public AuditorProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    public Result<ProductPageVO> search(@Valid ProductQueryDTO query) {
        return Result.success(productService.pageAdminProducts(query));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getAdminDetail(id));
    }

    @PutMapping("/{id}/force-offline")
    public Result<ProductVO> forceOffline(@PathVariable Long id, @RequestParam(required = false) String reason) {
        ProductVO product = productService.forceOfflineProduct(id);
        log.info("product force-offline productId={}, reason={}", id, reason);
        return Result.success(product);
    }
}
