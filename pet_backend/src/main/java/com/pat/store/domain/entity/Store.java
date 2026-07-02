package com.pat.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import com.pat.store.domain.dto.StoreDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private String closeReason;

    @TableLogic
    private Integer deleted;

    /**
     * 从 DTO 构建店铺实体（不含 userId 等上下文字段）。
     * 由 Service 注入 userId 后保存。
     *
     * @param dto 店铺创建/更新参数
     * @return 新构造的店铺实体
     */
    public static Store from(StoreDTO dto) {
        Store s = new Store();
        s.setStoreName(dto.getStoreName());
        s.setStoreLogo(dto.getStoreLogo());
        s.setStorePhone(dto.getStorePhone());
        s.setStoreDesc(dto.getStoreDesc());
        s.setProvince(dto.getProvince());
        s.setCity(dto.getCity());
        s.setDistrict(dto.getDistrict());
        s.setAddress(dto.getAddress());
        s.setLongitude(dto.getLongitude());
        s.setLatitude(dto.getLatitude());
        s.setStatus(dto.getStatus());
        return s;
    }

}