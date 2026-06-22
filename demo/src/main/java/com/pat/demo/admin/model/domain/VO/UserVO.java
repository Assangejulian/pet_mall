package com.pat.demo.admin.model.domain.VO;

import lombok.Data;

import java.util.Date;

@Data
public class UserVO {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 用户账号
     */
    private String userAccount;
    
    /**
     * 用户头像
     */
    private String avatarUrl;
    
    /**
     * 性别
     */
    private Integer gender;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 用户状态 0-正常
     */
    private Integer userStatus;
    
    /**
     * 用户角色 0-普通用户 1-管理员
     */
    private Integer userRole;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
