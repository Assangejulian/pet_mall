package com.pat.order.domain.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {

    PENDING_PAY(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    RECEIVED(3, "已收货"),
    EVALUATED(4, "已评价"),
    CANCELLED(-1, "已取消"),
    REFUNDING(-2, "申请退款"),
    REFUNDED(-3, "已退款"),
    REJECTED(-4, "直接退款");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus of(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}
