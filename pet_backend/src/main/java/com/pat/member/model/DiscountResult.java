package com.pat.member.model;

import java.math.BigDecimal;

public class DiscountResult {
    private final BigDecimal totalAmount;
    private final BigDecimal discountAmount;
    private final BigDecimal payAmount;
    private final String desc;

    public DiscountResult(BigDecimal totalAmount, BigDecimal discountAmount, BigDecimal payAmount, String desc) {
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.payAmount = payAmount;
        this.desc = desc;
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getPayAmount() { return payAmount; }
    public String getDesc() { return desc; }
}