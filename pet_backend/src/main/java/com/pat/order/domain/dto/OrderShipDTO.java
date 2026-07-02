package com.pat.order.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员发货 DTO
 */
@Data
public class OrderShipDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private String logisticsNo;
    private String carrier;
}
