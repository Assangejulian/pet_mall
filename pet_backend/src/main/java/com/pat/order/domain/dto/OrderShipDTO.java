package com.pat.order.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员发货 DTO
 */
@Data
public class OrderShipDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "物流单号不能为空")
    @Size(max = 100, message = "物流单号长度不能超过100")
    private String logisticsNo;

    @NotBlank(message = "物流公司不能为空")
    @Size(max = 100, message = "物流公司长度不能超过100")
    private String carrier;
}
