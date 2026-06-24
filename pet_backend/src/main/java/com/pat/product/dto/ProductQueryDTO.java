package com.pat.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "商品分页查询参数")
public class ProductQueryDTO {

    // 用户端沿用 pageNum/pageSize/productName/productType。
    // 后台 pet_admin 组件使用 page/size/keyword/type。
    // 两套参数都保留，方便团队不同页面直接接入同一个后端。

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码，默认1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    @Schema(description = "每页数量，默认10，最大100")
    private Long pageSize = 10L;

    @Size(max = 200, message = "商品名称长度不能超过200")
    @Schema(description = "商品名称模糊查询")
    private String productName;

    @Size(max = 200, message = "关键词长度不能超过200")
    @Schema(description = "管理端关键词，兼容 pet_admin keyword")
    private String keyword;

    @Schema(description = "商店ID")
    private Long storeId;

    @Min(value = 1, message = "商品类型只能为1或2")
    @Max(value = 2, message = "商品类型只能为1或2")
    @Schema(description = "商品类型：1-宠物，2-宠物周边")
    private Integer productType;

    @Schema(description = "管理端类型，兼容 pet_admin type")
    private String type;

    @Size(max = 50, message = "分类长度不能超过50")
    @Schema(description = "分类")
    private String category;

    @Min(value = 0, message = "商品状态只能为0、1或2")
    @Max(value = 2, message = "商品状态只能为0、1或2")
    @Schema(description = "商品状态：0-下架，1-上架，2-已售出")
    private Integer status;

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "管理端页码，兼容 pet_admin page")
    private Long page;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    @Schema(description = "管理端每页数量，兼容 pet_admin size")
    private Long size;
}
