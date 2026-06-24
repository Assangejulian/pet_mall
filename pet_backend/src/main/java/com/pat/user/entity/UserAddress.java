package com.pat.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_address")
public class UserAddress extends BaseEntity {
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer defaulted;
}
