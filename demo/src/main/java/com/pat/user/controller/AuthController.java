package com.pat.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.common.domain.Result;
import com.pat.common.utils.JwtUtil;
import com.pat.user.dto.LoginDTO;
import com.pat.user.entity.User;
import com.pat.user.service.UserService;
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
    private UserService userService;

    @PostMapping("/api/user/login")
    public Result<Map<String, Object>> userLogin(@Valid @RequestBody LoginDTO dto) {
        return doLogin(dto, "user", JwtUtil.USER_EXPIRE, "用户不存在", false);
    }

    @PostMapping("/api/admin/login")
    public Result<Map<String, Object>> adminLogin(@Valid @RequestBody LoginDTO dto) {
        return doLogin(dto, "admin", JwtUtil.ADMIN_EXPIRE, "管理员账号不存在", true);
    }

    private Result<Map<String, Object>> doLogin(LoginDTO dto, String role, long expireMs, String notFoundMsg, boolean includeRole) {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getRole, role));

        if (user == null) return Result.error(notFoundMsg);
        if (!dto.getPassword().equals(user.getPassword())) return Result.error("密码错误");
        if (user.getStatus() != 1) return Result.error("账号已禁用");

        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), expireMs);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        if (includeRole) data.put("role", user.getRole());
        return Result.success(data);
    }
}