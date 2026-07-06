package com.pat.product.helper;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.enums.ProductStatus;

import java.util.Map;
import java.util.Set;

/**
 * 商品状态机：校验状态流转是否合法
 *
 * <pre>
 * 下架(0) → 上架(1)
 * 上架(1) → 下架(0) / 已售出(2)
 * 已售出(2) → 下架(0)
 * </pre>
 */
public final class ProductStateMachine {

    private static final Map<ProductStatus, Set<ProductStatus>> RULES = Map.of(
            ProductStatus.OFFLINE, Set.of(ProductStatus.ONLINE),
            ProductStatus.ONLINE,  Set.of(ProductStatus.OFFLINE, ProductStatus.SOLD),
            ProductStatus.SOLD,    Set.of(ProductStatus.OFFLINE)
    );

    private ProductStateMachine() {}

    public static void validate(Integer currentCode, Integer targetCode) {
        ProductStatus current = ProductStatus.of(currentCode);
        ProductStatus target  = ProductStatus.of(targetCode);
        if (current == target) return;
        Set<ProductStatus> allowed = RULES.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR,
                    "商品状态非法: " + current.getDesc() + " → " + target.getDesc());
        }
    }
}