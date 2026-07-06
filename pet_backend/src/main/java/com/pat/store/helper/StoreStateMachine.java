package com.pat.store.helper;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.store.domain.enums.StoreStatus;

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

    private static final Map<StoreStatus, Set<StoreStatus>> RULES = Map.of(
            StoreStatus.PENDING,  Set.of(StoreStatus.OPEN, StoreStatus.REJECTED),
            StoreStatus.OPEN,     Set.of(StoreStatus.CLOSED),
            StoreStatus.CLOSED,   Set.of(StoreStatus.OPEN),
            StoreStatus.REJECTED, Set.of(StoreStatus.PENDING)
    );

    private StoreStateMachine() {}

    public static void validate(Integer currentCode, Integer targetCode) {
        StoreStatus current = StoreStatus.of(currentCode);
        StoreStatus target  = StoreStatus.of(targetCode);
        if (current == target) return;
        Set<StoreStatus> allowed = RULES.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR,
                    "店铺状态非法: " + current.getDesc() + " → " + target.getDesc());
        }
    }
}