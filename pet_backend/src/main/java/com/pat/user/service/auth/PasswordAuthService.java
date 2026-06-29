package com.pat.user.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.common.utils.PasswordEncoder;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 账号密码认证，支持用户名或手机号登录
 */
@Service("passwordAuthService")
public class PasswordAuthService implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User authenticate(LoginDTO dto) {
        validateInput(dto);
        User user = queryUserByLogin(dto);
        checkUserStatus(user);
        verifyPassword(user, dto.getPassword());
        return user;
    }

    private void validateInput(LoginDTO dto) {
        String username = dto.getUsername();
        String phone = dto.getPhone();
        if ((username == null || username.isBlank()) && (phone == null || phone.isBlank())) {
            throw new RuntimeException("用户名或手机号不能为空");
        }
    }

    private User queryUserByLogin(LoginDTO dto) {
        String phone = dto.getPhone();
        String username = dto.getUsername();

        // 优先按手机号查询
        if (phone != null && !phone.isBlank()) {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
            if (user != null) return user;
        }
        // 手机号未命中则按用户名查询
        if (username != null && !username.isBlank()) {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            if (user != null) return user;
        }
        throw new RuntimeException("用户不存在");
    }

    private void checkUserStatus(User user) {
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已禁用");
        }
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
            throw new RuntimeException("密码错误");
        }
    }
}
