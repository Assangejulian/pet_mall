package com.pat.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.user.domain.entity.User;
import com.pat.user.mapper.UserMapper;
import com.pat.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User createUser(User user) {
        if (user.getPassword() == null) user.setPassword("");
        if (user.getRole() == null) user.setRole("user");
        if (user.getStatus() == null) user.setStatus(1);
        save(user);
        return user;
    }

    @Override
    public void checkUserActive(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
    }

    @Override
    public User findOrCreateByWechat(String openid, String unionid) {
        User user = findUserByWechat(openid, unionid);
        if (user != null) {
            syncUnionid(user, unionid);
            return user;
        }
        return createWechatUser(openid, unionid);
    }

    private User findUserByWechat(String openid, String unionid) {
        if (unionid != null && !unionid.isBlank()) {
            User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUnionid, unionid));
            if (user != null) return user;
        }
        return getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }

    private void syncUnionid(User user, String unionid) {
        if (unionid != null && !unionid.isBlank() && user.getUnionid() == null) {
            user.setUnionid(unionid);
            updateById(user);
        }
    }

    private User createWechatUser(String openid, String unionid) {
        User user = new User();
        user.setUsername("wx_" + (openid.length() > 8 ? openid.substring(0, 8) : openid));
        user.setOpenid(openid);
        user.setUnionid(unionid);
        createUser(user);
        return user;
    }
}
