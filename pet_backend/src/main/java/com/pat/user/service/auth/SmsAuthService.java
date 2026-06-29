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
 * 手机验证码登录
 * authType = sms
 */
@Service("smsAuthService")
public class SmsAuthService implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CODE_PREFIX = "sms:code:";

    @Override
    public User authenticate(LoginDTO dto) {
        String phone = dto.getPhone();
        String code = dto.getCode();
        if (phone == null || code == null) {
            throw new BusinessException(ErrorCode.CODE_EMPTY);
        }
        verifySmsCode(phone, code);
        User user = findOrCreateUserByPhone(dto.getPhone());
        userService.checkUserActive(user);
        return user;
    }

    private void verifySmsCode(String phone, String code) {
        String key = CODE_PREFIX + phone;
        String saved = redisTemplate.opsForValue().get(key);
        if (saved == null || !saved.equals(code)) {
            throw new BusinessException(ErrorCode.CODE_WRONG);
        }
        redisTemplate.delete(key);
    }

    private User findOrCreateUserByPhone(String phone) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            user = new User();
            user.setUsername(phone);
            user.setPhone(phone);
            userService.createUser(user);
        }
        return user;
    }


}
