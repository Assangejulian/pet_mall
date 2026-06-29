package com.pat.store.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NearbyStoreRow {
    private Long id;
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
    private BigDecimal distanceKm;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
