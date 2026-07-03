package com.pat.order.domain.enums;

import com.pat.common.exception.BusinessException;
import com.pat.common.domain.ErrorCode;
import lombok.Getter;

/**
 * 订单状态枚举。
 * <p>正数表示正常流程状态，负数表示异常/退款状态。</p>
 */
@Getter
public enum OrderStatus {

    /** 待支付（初始状态） */
    PENDING_PAY(0, "待支付"),
    /** 已支付（等待商家发货） */
    PAID(1, "已支付"),
    /** 已发货（等待用户确认收货） */
    SHIPPED(2, "已发货"),
    /** 已收货（交易完成） */
    RECEIVED(3, "已收货"),
    /** 已评价 */
    EVALUATED(4, "已评价"),
    /** 已取消（未支付前取消） */
    CANCELLED(-1, "已取消"),
    /** 申请退款（用户发起） */
    REFUNDING(-2, "申请退款"),
    /** 已退款 */
    REFUNDED(-3, "已退款"),
    /** 直接退款（管理端操作） */
    REJECTED(-4, "直接退款");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举。
     *
     * @param code 状态码
     * @return 对应的枚举，未找到返回 null
     */
    public static OrderStatus of(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) { return s; }
        }
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "未知订单状态: " + code);
    }
}