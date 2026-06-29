package com.pat.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(description = "支付结果")
public class OrderPaymentVO {
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付状态")
    private Integer status;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;
}
