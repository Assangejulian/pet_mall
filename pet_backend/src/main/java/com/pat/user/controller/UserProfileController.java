package com.pat.user.controller;

import com.pat.member.calculator.MemberDiscountCalculator;
import com.pat.common.domain.Result;
import com.pat.common.util.UserHolder;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
        map.put("avatar", user.getAvatar());
        map.put("memberLevel", user.getMemberLevel());
        map.put("levelName", discountCalculator.getLevelName(user.getMemberLevel()));
        map.put("discountDesc", discountCalculator.getDiscountDesc(user.getMemberLevel()));
        return Result.success(map);
    }
}
