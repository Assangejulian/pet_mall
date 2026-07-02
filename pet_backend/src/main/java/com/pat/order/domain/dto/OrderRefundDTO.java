package com.pat.order.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退款审核 DTO
 */
@Data
public class OrderRefundDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;   // true=退款通过 → -3, false=驳回 → 回到已收货(3)

    private String rejectReason;
}
