package com.pat.order.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderEvaluateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private List<ItemEvaluate> items;

    @Data
    public static class ItemEvaluate {
        @NotNull(message = "订单项ID不能为空")
        private Long orderItemId;
        private String content;
    }
}
