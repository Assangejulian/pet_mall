package com.pat.demo.admin.service.impl;

import com.artsail.common.domain.ErrorCode;
import com.artsail.common.exception.BusinessException;
import com.artsail.admin.model.domain.Query.UserQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.artsail.admin.model.domain.User;
import com.artsail.admin.service.UserService;
import com.artsail.admin.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import lombok.extern.slf4j.Slf4j;


import java.util.List;

import static com.artsail.admin.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author 13372
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-03-12 17:19:06
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Autowired
    private UserMapper userMapper;

    private static final String SALT = "artSail";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        log.info("开始注册，账号：{}", userAccount);

        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {

            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR,"参数为空");
        }

        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.FARAMS_ERROR,"账号长度不足 ");
        }

        if(userPassword.length()<4||checkPassword.length()<4){

            throw new BusinessException(ErrorCode.FARAMS_ERROR,"密码长度不足");
        }


        //账户不能包含特殊字符
        String validPattern = "^[a-zA-Z0-9]+$";
        if(!userAccount.matches(validPattern)){
            log.info("失败：账号包含特殊字符");
            throw new BusinessException(ErrorCode.FARAMS_ERROR,"账号包含特殊字符");
        }

        //密码和校验密码相同
        if(!userPassword.equals(checkPassword)){
            log.info("失败：两次密码不一致");
            throw new BusinessException(ErrorCode.FARAMS_ERROR);
        }

        //账户不能重复
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        long count = this.count(queryWrapper);
        if(count>0){
            log.info("失败：账号已存在，数量：{}", count);
            throw new BusinessException(ErrorCode.FARAMS_ERROR);
        }


        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());



        //3.插入数据
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptPassword)
                .userStatus(0)
                .isDelete(0)
                .build();

        log.info("准备保存用户：{}", user.getUserAccount());
        boolean saveResult = this.save(user);
        log.info("保存结果：{}, 用户 ID: {}", saveResult, user.getId());

        if(!saveResult){
            throw  new BusinessException(ErrorCode.FARAMS_ERROR);
        }

        return user.getId();

    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        log.info("开始登录，账号：{}", userAccount);

        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {

            //todo 修改为自定义异常
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR,"参数为空");

        }

        if(userAccount.length()<4){
            log.info("失败：账号长度不足 ");
            throw new BusinessException(ErrorCode.FARAMS_ERROR,"账号长度不足 ");
        }

        if(userPassword.length()<4){
            log.info("失败：密码长度不足");
            throw new BusinessException(ErrorCode.FARAMS_ERROR,"密码长度不足");
        }

        //账户不能包含特殊字符
        String validPattern = "^[a-zA-Z0-9]+$";
        if(!userAccount.matches(validPattern)){
            log.info("失败：账号包含特殊字符");
            throw new BusinessException(ErrorCode.FARAMS_ERROR,"账号包含特殊字符");
        }


        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount).eq("user_password",encryptPassword);
        User user = this.getOne(queryWrapper);
        if(user==null){
            throw new BusinessException(ErrorCode.FARAMS_ERROR);
        }

        //用户脱敏
        User safetyUser=userDTOtoVO(user);

        //记录用户的登入态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);

        return safetyUser;


    }


    @Override
    public boolean userLogout(HttpServletRequest request) {
        log.info("开始登出");
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;

    }


    @Override
    public List<User> getUserLists(UserQuery userQuery)
    {
        
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 如果 username 不为空，则进行模糊查询
        if (StringUtils.isNotBlank(userQuery.getUserName())){
            queryWrapper.like("user_name",userQuery.getUserName());
        }
        if (userQuery.getUserAccount()!=null){
            queryWrapper.eq("user_account",userQuery.getUserAccount());
        }
        if(userQuery.getUserRole()!=null)
        {
            queryWrapper.eq("user_role",userQuery.getUserRole());
        }
        if (userQuery.getCreateTime()!= null){
            queryWrapper.ge("create_time",userQuery.getCreateTime());
        }
        queryWrapper.eq("is_delete",0);
        queryWrapper.orderByDesc("id");
        
        
        return this.list(queryWrapper);

    }


   private User userDTOtoVO(User user){
        //用户脱敏
      User safetyUser=User.builder()
                .id(user.getId())
                .avatarUrl(user.getAvatarUrl())
                .userRole(user.getUserRole())
                .userName(user.getUserName())
                .userAccount(user.getUserAccount())
                .gender(user.getGender())
                .userStatus(user.getUserStatus())
                .phone(user.getPhone())
                .email(user.getEmail())
                .createTime(user.getCreateTime())
                .build();
        return safetyUser;

    }

}




