package com.pat.store.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store")
public class Store extends BaseEntity {
    private Long userId;
    private String storeName;
    private String storeLogo;
    private String storePhone;
    private String storeDesc;
    private String province;
    private String city;
    private String district;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer status;

    @TableLogic
    private Integer deleted;
}
