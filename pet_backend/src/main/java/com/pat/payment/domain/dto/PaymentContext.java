package com.pat.payment.domain.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentContext {
    private String orderNo;
    private BigDecimal totalAmount;
    private String subject;
    private String description;
    private String openid;
}
