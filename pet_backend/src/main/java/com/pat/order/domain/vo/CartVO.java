package com.pat.order.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.pat.product.domain.vo.ProductVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "购物车返回对象")
public class CartVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "购物车记录ID")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "是否选中 1-选中 0-未选")
    private Integer checked;

    @Schema(description = "商品信息")
    private ProductVO productInfo;
}
