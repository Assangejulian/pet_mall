package com.pat.product.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ProductStoreVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String storeName;
    private String storeLogo;
    private String storePhone;
    private String province;
    private String city;
    private String district;
    private String address;
    private Integer status;
    private String statusText;
}
