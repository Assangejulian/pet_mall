package com.pat.user.helper;

import com.pat.common.constant.RedisConstants;
import com.pat.user.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码发送 Helper
 */
@Slf4j
@Component
public class SmsHelper {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成并缓存验证码，打印日志（TODO: 接入真实短信网关后替换）
     */
    public String sendAndCache(String phone) {
        String code = MailUtils.generateCode(6);
        String key = RedisConstants.SMS_CODE_KEY + phone;
        redisTemplate.opsForValue().set(key, code, RedisConstants.SMS_CODE_TTL, TimeUnit.MINUTES);
        log.info("[SMS] verification code {} sent to {} (TODO: integrate SMS gateway)", code, phone);
        return code;
    }
}
