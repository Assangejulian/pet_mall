package com.pat.user.controller;

import com.pat.common.domain.Result;
import com.pat.common.utils.JwtUtil;
import com.pat.user.dto.LoginDTO;
import com.pat.user.dto.LoginVO;
import com.pat.user.entity.User;
import com.pat.user.service.auth.AuthServiceRouter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一登录入口
 * <p>根据 LoginDTO.authType 路由到对应认证策略: password | sms | wechat | face | email_code</p>
 */
@RestController
public class AuthController {

    @Autowired
    private AuthServiceRouter authServiceRouter;

    /** 用户登录（多策略），默认 password */
    @PostMapping("/api/user/login")
    public Result<LoginVO> userLogin(@Valid @RequestBody LoginDTO dto) {
        String authType = dto.getAuthType();
        if (authType == null || authType.isBlank()) authType = "password";
        dto.setAuthType(authType);
        User user = authServiceRouter.getService(authType).authenticate(dto);
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.USER_EXPIRE);
        return Result.success(new LoginVO(token, user.getId(), user.getUsername()));
    }

    /** 管理员登录，强制密码认证 */
    @PostMapping("/api/admin/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody LoginDTO dto) {
        dto.setAuthType("password");
        User user = authServiceRouter.getService("password").authenticate(dto);
        if (!"admin".equals(user.getRole())) return Result.error("无管理员权限");
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.ADMIN_EXPIRE);
        return Result.success(new LoginVO(token, user.getId(), user.getUsername()));
    }
}