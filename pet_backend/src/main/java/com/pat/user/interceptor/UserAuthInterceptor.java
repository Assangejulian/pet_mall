package com.pat.user.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.Result;
import com.pat.user.utils.JwtUtil;
import com.pat.user.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader(AUTH_HEADER);
        if (auth == null || !auth.startsWith(BEARER_PREFIX)) {
            writeJson(response, 401, "未登录或token无效");
            return false;
        }

        String token = auth.substring(BEARER_PREFIX.length());
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            writeJson(response, 401, "token已过期或无效");
            return false;
        }

        // 注入用户上下文
        UserHolder.save("userId", claims.get("userId", Long.class));
        UserHolder.save("username", claims.get("username", String.class));
        UserHolder.save("role", claims.get("role", String.class));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.remove();
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), Result.error(code, message));
    }
}
