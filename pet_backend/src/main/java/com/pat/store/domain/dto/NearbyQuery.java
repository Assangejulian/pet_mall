package com.pat.store.domain.dto;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "附近门店查询参数")
public class NearbyQuery {

    private static final BigDecimal DEFAULT_RADIUS_KM = BigDecimal.TEN;
    private static final BigDecimal MAX_RADIUS_KM = BigDecimal.valueOf(100);
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

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

    @DecimalMin(value = "0.0000001", message = "搜索半径必须大于0")
    @DecimalMax(value = "100.0", message = "搜索半径不能超过100公里")
    @Schema(description = "搜索半径(公里)，优先使用", example = "10")
    private BigDecimal radiusKm;

    @DecimalMin(value = "0.0000001", message = "搜索半径必须大于0")
    @DecimalMax(value = "100.0", message = "搜索半径不能超过100公里")
    @Schema(description = "兼容旧参数：搜索半径(公里)", example = "10")
    private BigDecimal radius;

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "当前页，默认1")
    private Long current = DEFAULT_CURRENT;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    @Schema(description = "每页数量，默认20，最大100")
    private Long size = DEFAULT_SIZE;

    @Size(max = 100, message = "关键词长度不能超过100")
    @Schema(description = "门店名称关键词")
    private String keyword;

    @Size(max = 50, message = "城市长度不能超过50")
    @Schema(description = "城市")
    private String city;

    public BigDecimal resolvedRadiusKm() {
        BigDecimal resolved = radiusKm != null ? radiusKm : radius;
        if (resolved == null) {
            resolved = DEFAULT_RADIUS_KM;
        }
        if (resolved.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "查询半径必须大于0");
        }
        if (resolved.compareTo(MAX_RADIUS_KM) > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "查询半径不能超过100公里");
        }
        return resolved;
    }

    public long resolvedCurrent() {
        if (current == null) {
            return DEFAULT_CURRENT;
        }
        if (current <= 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "页码不能小于1");
        }
        return current;
    }

    public long resolvedSize() {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size <= 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "每页数量不能小于1");
        }
        if (size > MAX_SIZE) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "每页数量不能超过100");
        }
        return size;
    }

    public void validateRequiredCoordinates() {
        if (longitude == null || latitude == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "经纬度不能为空");
        }
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "经度范围必须在-180到180之间");
        }
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "纬度范围必须在-90到90之间");
        }
    }

    public void validate() {
        validateRequiredCoordinates();
        resolvedRadiusKm();
        resolvedCurrent();
        resolvedSize();
        if (keyword != null && keyword.length() > 100) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "关键词长度不能超过100");
        }
        if (city != null && city.length() > 50) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "城市长度不能超过50");
        }
    }
}
