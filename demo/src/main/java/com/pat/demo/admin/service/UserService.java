package com.pat.demo.admin.service;

import com.artsail.admin.model.domain.Query.UserQuery;
import com.artsail.admin.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


/**
* @author 13372
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-03-12 17:19:06
*/

public interface UserService extends IService<User> {




    /**
     *  用户注册
     *  @param userAccount 用户账户
     *  @param userPassword 用户密码
     *  @param checkPassword 校验密码
     *   @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     *  用户登录
     *  @param userAccount 用户账户
     *  @param userPassword 用户密码
     *  @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     *  用户注销
     *  @param request
     *  @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 查询用户
     *
     */
    List<User> getUserLists(UserQuery userQuery);


}
