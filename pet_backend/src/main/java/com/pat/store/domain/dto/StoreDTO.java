package com.pat.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商店参数，合并新增、修改和查询条件")
public class StoreDTO {

    @Schema(description = "批量更新时使用的商店ID")
    private Long id;

    @Schema(description = "店主用户ID")
    private Long userId;

    @Size(max = 100, message = "商店名称长度不能超过100")
    @Schema(description = "商店名称")
    private String storeName;

    @Size(max = 100, message = "关键词长度不能超过100")
    @Schema(description = "关键词，兼容 pet_admin keyword")
    private String keyword;

    @Size(max = 500, message = "商店Logo长度不能超过500")
    private String storeLogo;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String storePhone;

    private String storeDesc;

    @Size(max = 50, message = "省份长度不能超过50")
    private String province;

    @Size(max = 50, message = "城市长度不能超过50")
    private String city;

    @Size(max = 50, message = "区县长度不能超过50")
    private String district;

    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

    @DecimalMin(value = "-180.0", message = "经度不能小于-180")
    @DecimalMax(value = "180.0", message = "经度不能大于180")
    private BigDecimal longitude;

    @DecimalMin(value = "-90.0", message = "纬度不能小于-90")
    @DecimalMax(value = "90.0", message = "纬度不能大于90")
    private BigDecimal latitude;

    @Min(value = 0, message = "商店状态只能为0、1、2或3")
    @Max(value = 3, message = "商店状态只能为0、1、2或3")
    private Integer status;

    @Size(max = 500, message = "审核意见长度不能超过500")
    private String auditRemark;

    @Size(max = 500, message = "关闭原因长度不能超过500")
    private String closeReason;
}
