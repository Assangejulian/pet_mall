package com.pat.order.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付回调通知 DTO
 */
@Data
public class PayNotifyDTO {
    @NotBlank(message = "商户订单号不能为空")
    private String outTradeNo;
}
