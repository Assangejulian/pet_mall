package com.pat.member.calculator;

import com.pat.member.model.DiscountResult;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MemberDiscountCalculator {

    private static final BigDecimal RATE_NORMAL  = new BigDecimal("1.00");
    private static final BigDecimal RATE_SILVER  = new BigDecimal("0.95");
    private static final BigDecimal RATE_GOLD    = new BigDecimal("0.90");
    private static final BigDecimal RATE_DIAMOND = new BigDecimal("0.85");

    public DiscountResult calculate(BigDecimal total, Integer memberLevel) {
        if (total == null) total = BigDecimal.ZERO;
        BigDecimal rate = getRate(memberLevel);
        BigDecimal payAmount = total.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = total.subtract(payAmount);
        String desc = getDesc(rate);
        return new DiscountResult(total, discountAmount, payAmount, desc);
    }

    public String getDiscountDesc(Integer memberLevel) {
        return getDesc(getRate(memberLevel));
    }

    public String getLevelName(Integer memberLevel) {
        if (memberLevel == null) return "Normal";
        return switch (memberLevel) {
            case 1 -> "Silver";
            case 2 -> "Gold";
            case 3 -> "Diamond";
            default -> "Normal";
        };
    }

    private BigDecimal getRate(Integer memberLevel) {
        if (memberLevel == null) return RATE_NORMAL;
        return switch (memberLevel) {
            case 1 -> RATE_SILVER;
            case 2 -> RATE_GOLD;
            case 3 -> RATE_DIAMOND;
            default -> RATE_NORMAL;
        };
    }

    private String getDesc(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ONE) >= 0) return "No discount";
        int discount = rate.multiply(new BigDecimal("100")).intValue();
        return "Member " + (100 - discount) + "% off";
    }
}