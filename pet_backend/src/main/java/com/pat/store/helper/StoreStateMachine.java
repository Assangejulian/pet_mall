package com.pat.store.helper;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;

import java.util.Map;
import java.util.Set;

/**
 * 店铺状态机：校验状态流转是否合法
 *
 * <pre>
 * 待审核(0) → 营业中(1) / 审核驳回(3)
 * 营业中(1) → 已关闭(2)
 * 已关闭(2) → 营业中(1)
 * 审核驳回(3) → 待审核(0)
 * </pre>
 */
public final class StoreStateMachine {

    private static final Map<Integer, Set<Integer>> RULES = Map.of(
            0, Set.of(1, 3),   // 待审核 → 营业中 / 审核驳回
            1, Set.of(2),      // 营业中 → 已关闭
            2, Set.of(1),      // 已关闭 → 营业中（重新开业）
            3, Set.of(0)       // 审核驳回 → 待审核（重新提交）
    );

    private StoreStateMachine() {}

    /**
     * 校验店铺状态是否允许流转，非法流转抛出异常。
     *
     * @param currentCode 当前状态
     * @param targetCode 目标状态
     */
    public static void validate(Integer currentCode, Integer targetCode) {
        if (currentCode == null || targetCode == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "状态码不能为空");
        }
        if (currentCode.equals(targetCode)) {
            return;
        }
        Set<Integer> allowed = RULES.get(currentCode);
        if (allowed == null || !allowed.contains(targetCode)) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR,
                    "店铺状态非法: " + statusText(currentCode) + " → " + statusText(targetCode));
        }
    }

    private static String statusText(Integer code) {
        if (code == null) return "null";
        return switch (code) {
            case 0 -> "待审核";
            case 1 -> "营业中";
            case 2 -> "已关闭";
            case 3 -> "审核驳回";
            default -> String.valueOf(code);
        };
    }
}