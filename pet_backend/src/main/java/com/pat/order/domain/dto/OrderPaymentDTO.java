package com.pat.order.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "支付请求")
public class OrderPaymentDTO {
    @NotBlank(message = "订单号不能为空")
    @Schema(description = "商户订单号")
    private String orderNo;

    @Schema(description = "支付方式：WECHAT/ALIPAY", example = "WECHAT")
    private String payMethod;
}
