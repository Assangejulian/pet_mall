package com.pat.user.service.auth;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.common.utils.PasswordEncoder;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 账号密码认证，支持用户名或手机号登录
 */
@Service("passwordAuthService")
public class PasswordAuthService implements AuthService {

    @Autowired
    private UserService userService;

    @Override
    public User authenticate(LoginDTO dto) {
        String username = dto.getUsername();
        String phone = dto.getPhone();
        if ((username == null || username.isBlank()) && (phone == null || phone.isBlank())) {
            throw new BusinessException(ErrorCode.USERNAME_PHONE_EMPTY);
        }
        User user = queryUserByLogin(dto);
        userService.checkUserActive(user);
        verifyPassword(user, dto.getPassword());
        return user;
    }

    private User queryUserByLogin(LoginDTO dto) {
        String phone = dto.getPhone();
        String username = dto.getUsername();

        // 优先按手机号查询
        if (phone != null && !phone.isBlank()) {
            User user = userService.getOne(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
            if (user != null) return user;
        }
        // 手机号未命中则按用户名查询
        if (username != null && !username.isBlank()) {
            User user = userService.getOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            if (user != null) return user;
        }
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    private void verifyPassword(User user, String rawPassword) {
        String dbPwd = user.getPassword();
        boolean matched;
        if (dbPwd.contains(":")) {
            matched = PasswordEncoder.matches(rawPassword, dbPwd);
        } else {
            matched = rawPassword.equals(dbPwd);
        }
        if (!matched) {
            throw new BusinessException(ErrorCode.PASSWORD_WRONG);
        }
    }
}
