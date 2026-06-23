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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    /** C端用户登录 */
    @PostMapping("/api/user/login")
    public Result<Map<String, Object>> userLogin(@Valid @RequestBody LoginDTO dto) {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getRole, "user"));

        if (user == null) return Result.error("用户不存在");
        // 简单密码比对（后续可升级 BCrypt）
        if (!dto.getPassword().equals(user.getPassword())) return Result.error("密码错误");
        if (user.getStatus() != 1) return Result.error("账号已禁用");

        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.USER_EXPIRE);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        return Result.success(data);
    }

    /** B端管理员登录 */
    @PostMapping("/api/admin/login")
    public Result<Map<String, Object>> adminLogin(@Valid @RequestBody LoginDTO dto) {
        User admin = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getRole, "admin"));

        if (admin == null) return Result.error("管理员账号不存在");
        if (!dto.getPassword().equals(admin.getPassword())) return Result.error("密码错误");
        if (admin.getStatus() != 1) return Result.error("账号已禁用");

        String token = JwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole(), JwtUtil.ADMIN_EXPIRE);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", admin.getId());
        data.put("username", admin.getUsername());
        data.put("role", admin.getRole());
        return Result.success(data);
    }
}