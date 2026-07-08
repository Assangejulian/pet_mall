package com.pat.member.constant;

import java.math.BigDecimal;

/**
 * 会员等级折扣配置
 * 0-普通  1-银卡  2-金卡  3-钻石
 * @author 13372
 */
public class MemberDiscount {

    private static final BigDecimal RATE_NORMAL = new BigDecimal("1.00");
    private static final BigDecimal RATE_SILVER = new BigDecimal("0.95");
    private static final BigDecimal RATE_GOLD   = new BigDecimal("0.90");
    private static final BigDecimal RATE_DIAMOND = new BigDecimal("0.85");

    public static BigDecimal getRate(Integer memberLevel) {
        if (memberLevel == null) return RATE_NORMAL;
        return switch (memberLevel) {
            case 1 -> RATE_SILVER;
            case 2 -> RATE_GOLD;
            case 3 -> RATE_DIAMOND;
            default -> RATE_NORMAL;
        };
    }

    public static String getLevelName(Integer memberLevel) {
        if (memberLevel == null) return "普通会员";
        return switch (memberLevel) {
            case 1 -> "银卡会员";
            case 2 -> "金卡会员";
            case 3 -> "钻石会员";
            default -> "普通会员";
        };
    }

    public static String getDiscountDesc(Integer memberLevel) {
        if (memberLevel == null || memberLevel == 0) return "无折扣";
        BigDecimal rate = getRate(memberLevel);
        int discount = rate.multiply(new BigDecimal("100")).intValue();
        return "享" + (100 - discount) + "折优惠";
    }
}
