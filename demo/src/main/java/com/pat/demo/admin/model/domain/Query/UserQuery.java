package com.pat.demo.admin.model.domain.Query;


import lombok.Data;
import lombok.Getter;

/**
 * 用户查询参数
 * @author 13372
 */

@Data
public class UserQuery {
    private String userName;
    private String userAccount;
    private String gender;
    private String phone;
    private String email;
    private String userRole;
    private String createTime;

}
