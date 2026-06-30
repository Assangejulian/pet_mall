package com.pat.user.controller;

import lombok.extern.slf4j.Slf4j;

import com.pat.common.constant.RedisConstants;
import com.pat.user.utils.MailUtils;
import com.pat.user.utils.SmsHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.domain.Result;
import com.pat.user.utils.JwtUtil;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.dto.LoginVO;
import com.pat.user.domain.entity.User;
import com.pat.user.service.auth.AuthServiceRouter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.Set;

@Slf4j
@RestController
@Tag(name = "用户认证", description = "登录接口：密码/邮箱验证码/微信/人脸")
public class AuthController {

    private static final Set<String> MANAGEMENT_ROLES = Set.of("merchant", "auditor", "admin");

    @Autowired
    private AuthServiceRouter authServiceRouter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MailUtils mailUtils;

    @Autowired
    private SmsHelper smsHelper;

    @Operation(summary = "用户登录（多策略）")
    @PostMapping("/api/user/login")
    public Result<LoginVO> userLogin(@Valid @RequestBody LoginDTO dto) {
        String authType = dto.getAuthType();
        if (authType == null || authType.isBlank()) authType = "password";
        dto.setAuthType(authType);
        User user = authServiceRouter.getService(authType).authenticate(dto);
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.USER_EXPIRE);
        return Result.success(new LoginVO(token, user.getId(), user.getUsername(), user.getRole()));
    }

    @Operation(summary = "管理端登录（强制密码）")
    @PostMapping("/api/admin/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody LoginDTO dto) {
        dto.setAuthType("password");
        User user = authServiceRouter.getService("password").authenticate(dto);
        if (user.getRole() == null || !MANAGEMENT_ROLES.contains(user.getRole())) {
            return Result.error(403, "无管理端登录权限");
        }
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.ADMIN_EXPIRE);
        return Result.success(new LoginVO(token, user.getId(), user.getUsername(), user.getRole()));
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/api/user/send-email-code")
    public Result<Void> sendEmailCode(@RequestBody LoginDTO dto) {
        String email = dto.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")) {
            return Result.error("请输入正确的邮箱地址");
        }
        String code = MailUtils.generateCode(6);
        String key = RedisConstants.LOGIN_CODE_KEY + email;
        redisTemplate.opsForValue().set(key, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        mailUtils.sendMail(email, "暖窝登录验证码",
                "<h3>暖窝登录验证码</h3><p>您的验证码为：<b style='font-size:24px;color:#e8927c'>" + code + "</b></p><p>有效期 " + RedisConstants.LOGIN_CODE_TTL + " 分钟，请勿泄露。</p>",
                true);
        return Result.success();
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/api/user/send-sms-code")
    public Result<Void> sendSmsCode(@RequestBody LoginDTO dto) {
        String phone = dto.getPhone();
        if (phone == null || phone.isBlank() || phone.length() < 11) {
            return Result.error("请输入正确的手机号");
        }
        String code = MailUtils.generateCode(6);
        String key = RedisConstants.SMS_CODE_KEY + phone;
        redisTemplate.opsForValue().set(key, code, RedisConstants.SMS_CODE_TTL, TimeUnit.MINUTES);
        log.info("[SMS] verification code {} sent to {} (TODO: integrate SMS gateway)", code, phone);
        return Result.success();
    }

}
