package com.pat.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private String username;
    private String password;
    private String phone;
    private String avatar;
    private String email;
    private Integer memberLevel;
    private String realName;
    private LocalDate birthday;
    private String role;
    private Integer status;

    /** 微信小程序 openid */
    private String openid;

    /** 微信开放平台 unionid（跨公众号/小程序统一标识） */
    private String unionid;

    @TableField(exist = false)
    private String roleName;
}
