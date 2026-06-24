package com.pat.user.service;

import com.pat.common.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private MailUtils mailUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SpringTemplateEngine templateEngine;

    private static final String CODE_PREFIX = "login:code:";
    private static final long CODE_TTL = 5;

    public void sendCode(String email) {
        String code = MailUtils.generateCode(6);
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, CODE_TTL, TimeUnit.MINUTES);

        Context ctx = new Context();
        ctx.setVariable("verificationCode", code);
        String html = templateEngine.process("email-template", ctx);

        mailUtils.sendMail(email, "宠物商店 - 登录验证码", html, true);
        log.info("验证码已发送到 {}", email);
    }

    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String saved = redisTemplate.opsForValue().get(key);
        if (saved != null && saved.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}