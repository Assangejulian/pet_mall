package com.pat.order.helper;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
/* 订单状态机：校验状态流转是否合法 */
public class OrderStateMachine {

    private static final Map<Integer, Set<Integer>> STATE_MACHINE = Map.of(
            0,  Set.of(1, -1),      // 待支付 → 已支付 / 已取消
            1,  Set.of(2),           // 已支付 → 已发货
            2,  Set.of(3, -2, -4),   // 已发货 → 已收货 / 申请退单 / 直接退单
            3,  Set.of(4),           // 已收货 → 已评价
            -2, Set.of(-3, 3)        // 申请退单 → 退单通过 / 驳回
    );

    /** 校验 current → target 是否合法，不合法抛异常 */
    public static void validate(Integer current, Integer target) {
        Set<Integer> allowed = STATE_MACHINE.get(current);
        if (allowed == null || !allowed.contains(target))
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "状态非法: " + current + " → " + target);
    }
}
