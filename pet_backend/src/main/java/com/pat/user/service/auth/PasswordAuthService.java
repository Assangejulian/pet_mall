package com.pat.user.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.common.utils.PasswordEncoder;
import com.pat.user.dto.LoginDTO;
import com.pat.user.entity.User;
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
        String username = dto.getUsername();
        String phone = dto.getPhone();
        if ((username == null || username.isBlank()) && (phone == null || phone.isBlank())) {
            throw new RuntimeException("用户名或手机号不能为空");
        }

        User user = null;
        // 优先按手机号查询
        if (phone != null && !phone.isBlank()) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        }
        // 手机号未命中则按用户名查询
        if (user == null && username != null && !username.isBlank()) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        }
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已禁用");
        }

        // 校验密码（兼容旧版明文）
        String dbPwd = user.getPassword();
        boolean matched;
        if (dbPwd.contains(":")) {
            matched = PasswordEncoder.matches(dto.getPassword(), dbPwd);
        } else {
            matched = dto.getPassword().equals(dbPwd);
        }
        if (!matched) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }
}
