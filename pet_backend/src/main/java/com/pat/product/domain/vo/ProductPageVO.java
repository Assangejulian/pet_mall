package com.pat.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页返回对象")
public class ProductPageVO {
    private List<ProductVO> records;
    private Long total;
    private Long page;
    private Long size;
}
