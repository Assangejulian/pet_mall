package com.pat.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private Long storeId;

    private String productName;

    private Integer productType;

    private String category;

    private String productDesc;

    private BigDecimal price;

    private Integer stock;

    private String mainImage;

    private String images;

    private Integer status;

    private Long videoId;

    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Integer deleted;
}
