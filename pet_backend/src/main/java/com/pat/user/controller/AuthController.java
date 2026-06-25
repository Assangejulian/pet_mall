package com.pat.user.controller;

import com.pat.common.constant.RedisConstants;
import com.pat.common.utils.MailUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.domain.Result;
import com.pat.common.utils.JwtUtil;
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

@RestController
@Tag(name = "用户认证", description = "登录接口：密码/邮箱验证码/微信/人脸")
public class AuthController {

    @Autowired
    private AuthServiceRouter authServiceRouter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MailUtils mailUtils;

    @Operation(summary = "用户登录（多策略）")
    @PostMapping("/api/user/login")
    public Result<LoginVO> userLogin(@Valid @RequestBody LoginDTO dto) {
        String authType = dto.getAuthType();
        if (authType == null || authType.isBlank()) authType = "password";
        dto.setAuthType(authType);
        User user = authServiceRouter.getService(authType).authenticate(dto);
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.USER_EXPIRE);
        return Result.success(new LoginVO(token, user.getId(), user.getUsername()));
    }

    @Operation(summary = "管理员登录（强制密码）")
    @PostMapping("/api/admin/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody LoginDTO dto) {
        dto.setAuthType("password");
        User user = authServiceRouter.getService("password").authenticate(dto);
        if (!"admin".equals(user.getRole())) return Result.error("无管理员权限");
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.ADMIN_EXPIRE);
        return Result.success(new LoginVO(token, user.getId(), user.getUsername()));
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
}