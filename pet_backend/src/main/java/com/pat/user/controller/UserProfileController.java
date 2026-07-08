package com.pat.user.controller;

import com.pat.member.calculator.MemberDiscountCalculator;
import com.pat.common.domain.Result;
import com.pat.common.util.UserHolder;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "用户端接口")
public class UserProfileController {

    private final UserService userService;
    private final MemberDiscountCalculator discountCalculator;

    public UserProfileController(UserService userService, MemberDiscountCalculator discountCalculator) {
        this.userService = userService;
        this.discountCalculator = discountCalculator;
    }

    @Operation(summary = "获取当前用户个人信息（含会员等级）")
    @GetMapping("/api/user/profile")
    public Result<Map<String, Object>> profile() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("birthday", user.getBirthday());
        map.put("avatar", user.getAvatar());
        map.put("memberLevel", user.getMemberLevel());
        map.put("levelName", discountCalculator.getLevelName(user.getMemberLevel()));
        map.put("discountDesc", discountCalculator.getDiscountDesc(user.getMemberLevel()));
        return Result.success(map);
    }

    @Operation(summary = "更新当前用户个人信息")
    @PutMapping("/api/user/profile")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getBirthday() != null) user.setBirthday(dto.getBirthday());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        userService.updateById(user);
        return Result.success(null);
    }

    @Data
    public static class ProfileUpdateDTO {
        private String realName;
        private String phone;
        private String email;
        private LocalDate birthday;
        private String avatar;
    }
}
