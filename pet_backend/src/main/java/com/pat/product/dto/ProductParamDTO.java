package com.pat.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商品管理参数，合并新增、修改和查询条件")
public class ProductParamDTO {

    @Schema(description = "批量更新时使用的商品ID")
    private Long id;

    @Schema(description = "所属商店ID")
    private Long storeId;

    @Size(max = 200, message = "商品名称长度不能超过200")
    @Pattern(regexp = ".*\\S.*", message = "商品名称不能为空")
    @Schema(description = "商品名称")
    private String productName;

    @Size(max = 200, message = "关键词长度不能超过200")
    @Schema(description = "关键词，兼容 pet_admin keyword")
    private String keyword;

    @Min(value = 1, message = "商品类型只能为1或2")
    @Max(value = 2, message = "商品类型只能为1或2")
    @Schema(description = "商品类型：1-宠物，2-宠物周边")
    private Integer productType;

    @Schema(description = "类型文本，兼容 pet_admin type")
    private String type;

    @Size(max = 50, message = "分类长度不能超过50")
    @Schema(description = "分类")
    private String category;

    @Schema(description = "商品描述")
    private String productDesc;

    @DecimalMin(value = "0.00", message = "价格不能小于0")
    @Schema(description = "价格")
    private BigDecimal price;

    @Min(value = 0, message = "库存不能小于0")
    @Schema(description = "库存")
    private Integer stock;

    @Size(max = 500, message = "主图URL长度不能超过500")
    @Schema(description = "主图URL")
    private String mainImage;

    @Size(max = 2000, message = "多图JSON长度不能超过2000")
    @Schema(description = "多图JSON数组字符串")
    private String images;

    @Schema(description = "状态：0/下架，1/上架；已售出状态由订单模块控制")
    private String status;

    @Min(value = 0, message = "商品状态只能为0、1或2")
    @Max(value = 2, message = "商品状态只能为0、1或2")
    @Schema(description = "查询状态：0-下架，1-上架，2-已售出")
    private Integer statusCode;
}
