package com.pat.common.interceptor;

import com.pat.common.utils.JwtUtil;
import com.pat.common.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token无效\"}");
            return false;
        }

        String token = auth.substring(7);
        if (!JwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token已过期或无效\"}");
            return false;
        }

        Claims claims = JwtUtil.parseToken(token);
        String role = claims.get("role", String.class);
        if (!"admin".equals(role)) {
            response.setStatus(403);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":403,\"message\":\"无管理员权限\"}");
            return false;
        }

        UserHolder.save("userId", claims.get("userId", Long.class));
        UserHolder.save("username", claims.get("username", String.class));
        UserHolder.save("role", role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.remove();
    }
}