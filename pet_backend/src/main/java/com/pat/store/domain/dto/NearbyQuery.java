package com.pat.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "附近门店查询参数")
public class NearbyQuery {

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度不能小于-90")
    @DecimalMax(value = "90.0", message = "纬度不能大于90")
    @Schema(description = "当前纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal latitude;

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度不能小于-180")
    @DecimalMax(value = "180.0", message = "经度不能大于180")
    @Schema(description = "当前经度", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal longitude;

    @Schema(description = "搜索半径(公里)", example = "5.0")
    private Double radius = 5.0;
}