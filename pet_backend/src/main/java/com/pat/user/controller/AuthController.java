package com.pat.user.controller;

import com.pat.common.domain.Result;
import com.pat.common.utils.JwtUtil;
import com.pat.user.dto.LoginDTO;
import com.pat.user.entity.User;
import com.pat.user.service.auth.AuthService;
import com.pat.user.service.auth.AuthServiceRouter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private AuthServiceRouter authServiceRouter;

    @PostMapping("user/login")
    public Result<Map<String, Object>> userLogin(@Valid @RequestBody LoginDTO dto) {
        return doLogin(dto, JwtUtil.USER_EXPIRE, false);
    }

    @PostMapping("admin/login")
    public Result<Map<String, Object>> adminLogin(@Valid @RequestBody LoginDTO dto) {
        // 管理员强制走密码认证
        dto.setAuthType("password");
        User user = authenticate(dto);
        if (!"admin".equals(user.getRole())) {
            return Result.error("无管理员权限");
        }
        return buildTokenResult(user, JwtUtil.ADMIN_EXPIRE, true);
    }

    private Result<Map<String, Object>> doLogin(LoginDTO dto, long expireMs, boolean includeRole) {
        User user = authenticate(dto);
        return buildTokenResult(user, expireMs, includeRole);
    }

    private User authenticate(LoginDTO dto) {
        // 默认 authType = password
        if (dto.getAuthType() == null || dto.getAuthType().isBlank()) {
            dto.setAuthType("password");
        }
        AuthService service = authServiceRouter.getService(dto.getAuthType());
        return service.authenticate(dto);
    }

    private Result<Map<String, Object>> buildTokenResult(User user, long expireMs, boolean includeRole) {
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), expireMs);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        if (includeRole) {
            data.put("role", user.getRole());
        }
        return Result.success(data);
    }
}
