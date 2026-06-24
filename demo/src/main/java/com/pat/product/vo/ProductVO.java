package com.pat.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "商品返回对象")
public class ProductVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;
    private String productName;
    private Integer productType;
    private String category;
    private String productDesc;
    private BigDecimal price;
    private Integer stock;
    private String mainImage;
    private String images;
    private String status;
    private Integer statusCode;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long videoId;

    private String name;
    private String type;
    private String detail;
    private String image;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
