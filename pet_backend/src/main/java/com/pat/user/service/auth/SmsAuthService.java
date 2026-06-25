package com.pat.user.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.user.dto.LoginDTO;
import com.pat.user.entity.User;
import com.pat.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 手机验证码登录
 * authType = sms
 */
@Service("smsAuthService")
public class SmsAuthService implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CODE_PREFIX = "sms:code:";

    @Override
    public User authenticate(LoginDTO dto) {
        String phone = dto.getPhone();
        String code = dto.getCode();
        if (phone == null || code == null) {
            throw new RuntimeException("手机号和验证码不能为空");
        }

        // 校验验证码
        String key = CODE_PREFIX + phone;
        String saved = redisTemplate.opsForValue().get(key);
        if (saved == null || !saved.equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        redisTemplate.delete(key);

        // 按手机号查用户，不存在则自动注册
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            user = new User();
            user.setUsername(phone);
            user.setPhone(phone);
            user.setPassword("");
            user.setRole("user");
            user.setStatus(1);
            userMapper.insert(user);
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已禁用");
        }
        return user;
    }
}
