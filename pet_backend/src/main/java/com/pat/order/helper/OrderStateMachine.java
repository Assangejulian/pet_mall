package com.pat.order.helper;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 订单状态机：校验状态流转是否合法
 * <p>用 OrderStatus 枚举替代魔法数字，状态规则定义一处可见</p>
 */
@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> STATE_MACHINE = Map.of(
            OrderStatus.PENDING_PAY, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.PAID,        Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED,     Set.of(OrderStatus.RECEIVED, OrderStatus.REFUNDING, OrderStatus.REJECTED),
            OrderStatus.RECEIVED,    Set.of(OrderStatus.EVALUATED),
            OrderStatus.REFUNDING,   Set.of(OrderStatus.REFUNDED, OrderStatus.RECEIVED)
    );

    /** 校验 current → target 是否合法，不合法抛异常 */
    public static void validate(Integer currentCode, Integer targetCode) {
        OrderStatus current = OrderStatus.of(currentCode);
        OrderStatus target = OrderStatus.of(targetCode);
        if (current == null || target == null) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "未知状态码: " + currentCode + " → " + targetCode);
        }
        Set<OrderStatus> allowed = STATE_MACHINE.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR,
                    "状态非法: " + current.getDesc() + "(" + currentCode + ") → " + target.getDesc() + "(" + targetCode + ")");
        }
    }
}
