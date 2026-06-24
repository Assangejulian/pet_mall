package com.pat.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.common.domain.Result;
import com.pat.common.utils.JwtUtil;
import com.pat.common.utils.RegexUtils;
import com.pat.user.entity.User;
import com.pat.user.service.EmailService;
import com.pat.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @PostMapping("/code")
    public Result<Void> sendCode(@RequestParam String email) {
        if (RegexUtils.isEmailInvalid(email)) {
            return Result.error("邮箱格式不正确");
        }
        emailService.sendCode(email);
        return Result.success();
    }

    @PostMapping("/email-login")
    public Result<Map<String, Object>> emailLogin(@RequestParam String email, @RequestParam String code) {
        if (RegexUtils.isEmailInvalid(email)) {
            return Result.error("邮箱格式不正确");
        }
        if (!emailService.verifyCode(email, code)) {
            return Result.error("验证码错误或已过期");
        }
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            user = new User();
            user.setUsername(email);
            user.setEmail(email);
            user.setPassword("");
            user.setRole("user");
            user.setStatus(1);
            userService.save(user);
        }
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.USER_EXPIRE);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        return Result.success(data);
    }
}