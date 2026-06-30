package com.pat.user.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 邮箱验证码登录
 * authType = email_code
 */
@Service("emailAuthService")
public class EmailAuthService implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CODE_PREFIX = "login:code:";

    @Override
    public User authenticate(LoginDTO dto) {
        String email = dto.getEmail();
        String code = dto.getCode();
        if (email == null || code == null) {
            throw new BusinessException(ErrorCode.CODE_EMPTY);
        }
        verifyEmailCode(email, code);
        User user = findOrCreateUserByEmail(dto.getEmail());
        userService.checkUserActive(user);
        return user;
    }

    private void verifyEmailCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String saved = redisTemplate.opsForValue().get(key);
        if (saved == null || !saved.equals(code)) {
            throw new BusinessException(ErrorCode.CODE_WRONG);
        }
        redisTemplate.delete(key);
    }

    private User findOrCreateUserByEmail(String email) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            user = new User();
            user.setUsername(email);
            user.setEmail(email);
            userService.createUser(user);
        }
        return user;
    }


}
