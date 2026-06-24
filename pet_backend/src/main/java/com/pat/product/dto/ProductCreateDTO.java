package com.pat.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商品新增参数")
public class ProductCreateDTO {

    @NotNull(message = "商店ID不能为空")
    @Schema(description = "所属商店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200")
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @NotNull(message = "商品类型不能为空")
    @Min(value = 1, message = "商品类型只能为1或2")
    @Max(value = 2, message = "商品类型只能为1或2")
    @Schema(description = "商品类型：1-宠物，2-宠物周边", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productType;

    @Size(max = 50, message = "分类长度不能超过50")
    @Schema(description = "分类")
    private String category;

    @Schema(description = "商品描述")
    private String productDesc;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stock;

    @Size(max = 500, message = "主图URL长度不能超过500")
    @Schema(description = "主图URL")
    private String mainImage;

    @Size(max = 2000, message = "多图JSON长度不能超过2000")
    @Schema(description = "多图JSON数组字符串")
    private String images;

    @Schema(description = "状态：0/下架，1/上架；只允许0、1、下架、上架；为空默认上架")
    private String status;

}

