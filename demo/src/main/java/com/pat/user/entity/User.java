package com.pat.user.entity;

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

    @TableField(exist = false)
    private String roleName;
}