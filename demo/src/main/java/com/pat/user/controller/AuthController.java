package com.pat.user.controller;

import com.pat.common.domain.Result;
import com.pat.common.utils.JwtUtil;
import com.pat.user.dto.LoginDTO;
import com.pat.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** C端用户登录 */
    @PostMapping("/api/user/login")
    public Result<Map<String, Object>> userLogin(@Valid @RequestBody LoginDTO dto) {
        User user = findUser(dto.getUsername(), "user");

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
        User admin = findUser(dto.getUsername(), "admin");

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

    private User findUser(String username, String role) {
        try {
            return jdbcTemplate.queryForObject("""
                            SELECT id, username, password, role, status
                            FROM user
                            WHERE username = ? AND role = ?
                            LIMIT 1
                            """,
                    (rs, rowNum) -> {
                        User user = new User();
                        user.setId(rs.getLong("id"));
                        user.setUsername(rs.getString("username"));
                        user.setPassword(rs.getString("password"));
                        user.setRole(rs.getString("role"));
                        user.setStatus(rs.getInt("status"));
                        return user;
                    },
                    username,
                    role);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }
}
