package com.pat.order.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员取消订单 DTO
 */
@Data
public class OrderCancelDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private String cancelReason;
}
