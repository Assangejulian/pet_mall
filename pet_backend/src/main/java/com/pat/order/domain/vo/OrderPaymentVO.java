package com.pat.order.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(description = "支付结果")
public class OrderPaymentVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付状态")
    private Integer status;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付表单HTML（支付宝手机网站支付返回）")
    private String payForm;

    @Schema(description = "支付页面URL（支付宝手机网站支付返回）")
    private String payUrl;
}
