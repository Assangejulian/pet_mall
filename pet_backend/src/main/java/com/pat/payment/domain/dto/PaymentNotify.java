package com.pat.payment.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentNotify {
    @NotBlank(message = "商户订单号不能为空")
    private String outTradeNo;
}
