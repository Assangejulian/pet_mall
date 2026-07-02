package com.pat.order.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "下单结果")
public class OrderCreateVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "订单ID")
    private Long orderId;
}
