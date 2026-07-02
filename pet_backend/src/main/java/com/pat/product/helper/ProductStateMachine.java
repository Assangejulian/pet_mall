package com.pat.product.helper;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;

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

    public static final int OFFLINE = 0;
    public static final int ONLINE  = 1;
    public static final int SOLD    = 2;

    private static final Map<Integer, Set<Integer>> RULES = Map.of(
            OFFLINE, Set.of(ONLINE),            // 下架 → 上架
            ONLINE,  Set.of(OFFLINE, SOLD),     // 上架 → 下架 / 已售出
            SOLD,    Set.of(OFFLINE)            // 已售出 → 下架（由平台操作）
    );

    private ProductStateMachine() {}

    /**
     * 校验商品状态是否允许从 currentCode 流转到 targetCode。
     * 非法流转抛出 BusinessException。
     *
     * @param currentCode 当前状态
     * @param targetCode 目标状态
     */
    public static void validate(Integer currentCode, Integer targetCode) {
        if (currentCode == null || targetCode == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商品状态码不能为空");
        }
        if (currentCode.equals(targetCode)) {
            return;
        }
        Set<Integer> allowed = RULES.get(currentCode);
        if (allowed == null || !allowed.contains(targetCode)) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR,
                    "商品状态非法: " + statusText(currentCode) + " → " + statusText(targetCode));
        }
    }

    private static String statusText(Integer code) {
        if (code == null) return "null";
        return switch (code) {
            case OFFLINE -> "下架";
            case ONLINE  -> "上架";
            case SOLD    -> "已售出";
            default      -> String.valueOf(code);
        };
    }
}