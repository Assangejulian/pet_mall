package com.pat.order.service.support;

import com.pat.member.calculator.MemberDiscountCalculator;
import com.pat.member.model.DiscountResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 订单金额计算。
 * <p>只负责：原价合计 → 会员折扣 → 实付金额。</p>
 */
@Service
public class OrderAmountCalculator {

    private final MemberDiscountCalculator memberDiscountCalculator;

    public OrderAmountCalculator(MemberDiscountCalculator memberDiscountCalculator) {
        this.memberDiscountCalculator = memberDiscountCalculator;
    }

    /**
     * 根据会员等级计算最终金额。
     *
     * @param total      商品原价合计
     * @param memberLevel 会员等级
     * @return 金额计算结果
     */
    public AmountResult calculate(BigDecimal total, Integer memberLevel) {
        DiscountResult dr = memberDiscountCalculator.calculate(total, memberLevel);
        return new AmountResult(total, dr.getDiscountAmount(), dr.getPayAmount());
    }

    public static class AmountResult {
        private final BigDecimal totalAmount;
        private final BigDecimal discountAmount;
        private final BigDecimal payAmount;

        public AmountResult(BigDecimal totalAmount, BigDecimal discountAmount, BigDecimal payAmount) {
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.payAmount = payAmount;
        }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getPayAmount() { return payAmount; }
    }
}
