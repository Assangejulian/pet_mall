package com.pat.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.domain.Result;
import com.pat.common.utils.JwtUtil;
import com.pat.common.utils.UserHolder;
import com.pat.user.entity.User;
import com.pat.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台 API（/api/admin/* 由 AdminAuthInterceptor 保护）
 */
@RestController
@Tag(name = "后台管理", description = "管理员信息")
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前管理员信息（用于页面刷新后恢复登录态）
     */
        @Operation(summary = "获取当前管理员信息")
@GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        User user = userService.getById(userId);
        if (user == null || !"admin".equals(user.getRole())) {
            return Result.error(403, "无管理员权限");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole());
        return Result.success(data);
    }
}