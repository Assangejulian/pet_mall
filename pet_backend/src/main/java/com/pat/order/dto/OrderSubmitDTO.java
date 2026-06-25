package com.pat.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderSubmitDTO {
    @NotNull(message = "地址ID不能为空")
    private Long addressId;

    // 如果为空，则拉取购物车中所有 checked=1 的商品
    private List<Long> cartIds;

    private String remark;
}
