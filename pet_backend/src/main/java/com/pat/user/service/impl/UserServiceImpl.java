package com.pat.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}
