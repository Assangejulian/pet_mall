package com.pat.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.utils.JwtUtil;
import com.pat.user.entity.User;
import com.pat.user.service.auth.WxAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * PC 端微信扫码登录回调（OAuth2 网页授权）
 * 用户扫码 → 微信回调此地址 → 登录成功重定向回前端
 */
@Tag(name = "微信登录", description = "微信扫码/小程序登录")
@Slf4j
@Controller
public class WxLoginController {

    @Autowired
    private WxAuthService wxAuthService;

    private static final String FRONTEND_URL = "http://localhost:5173";

    @Operation(summary = "微信扫码登录/小程序登录")
    @GetMapping("/wxLogin")
    public String wxLogin(@RequestParam String code,
                          @RequestParam(required = false) String state,
                          HttpServletResponse response) throws IOException {
        log.debug("微信扫码回调, code:{}, state:{}", code, state);
        try {
            User user = wxAuthService.pcScanLogin(code);
            // 生成 JWT，重定向回前端
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), JwtUtil.USER_EXPIRE);
            String redirectUrl = String.format("%s/login?token=%s&username=%s&authType=wx",
                    FRONTEND_URL,
                    URLEncoder.encode(token, StandardCharsets.UTF_8),
                    URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            log.error("微信扫码登录失败", e);
            return "redirect:" + FRONTEND_URL + "/login?error=wx_auth_failed";
        }
    }
}
