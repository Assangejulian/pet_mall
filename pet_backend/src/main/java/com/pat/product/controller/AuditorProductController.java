package com.pat.product.controller;

import com.pat.common.domain.Result;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.domain.vo.ProductPageVO;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.service.ProductService;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.common.util.UserHolder;
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
        String value = requireReason(reason);
        ProductVO product = productService.forceOfflineProduct(id, value, UserHolder.getUserId());
        log.info("product force-offline productId={}, reason={}", id, value);
        return Result.success(product);
    }

    @PutMapping("/{id}/release-offline")
    public Result<ProductVO> releaseOffline(@PathVariable Long id) {
        ProductVO product = productService.releaseOfflineRestriction(id);
        log.info("product release-offline productId={}, auditorId={}", id, UserHolder.getUserId());
        return Result.success(product);
    }

    private String requireReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "强制下架原因不能为空");
        }
        String value = reason.trim();
        if (value.length() > 500) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "强制下架原因长度不能超过500");
        }
        return value;
    }
}
