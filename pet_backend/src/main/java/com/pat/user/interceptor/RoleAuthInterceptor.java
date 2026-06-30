package com.pat.user.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.Result;
import com.pat.user.utils.JwtUtil;
import com.pat.user.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/** Shared JWT parsing and role enforcement for management endpoints. */
public class RoleAuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;
    private final Set<String> allowedRoles;

    protected RoleAuthInterceptor(ObjectMapper objectMapper, Set<String> allowedRoles) {
        this.objectMapper = objectMapper;
        this.allowedRoles = Set.copyOf(allowedRoles);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            writeJson(response, 401, "未登录或token无效");
            return false;
        }

        Claims claims;
        try {
            claims = JwtUtil.parseToken(authorization.substring(BEARER_PREFIX.length()));
        } catch (Exception e) {
            writeJson(response, 401, "token已过期或无效");
            return false;
        }

        String role = claims.get("role", String.class);
        if (role == null || !allowedRoles.contains(role)) {
            writeJson(response, 403, "无访问权限");
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

    private void writeJson(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), Result.error(code, message));
    }
}
