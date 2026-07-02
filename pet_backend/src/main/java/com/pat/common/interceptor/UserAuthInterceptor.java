package com.pat.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.Result;
import com.pat.common.util.JwtUtil;
import com.pat.common.util.UserHolder;
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
        if (isPublicVideoRead(request)) {
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

    private boolean isPublicVideoRead(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        if (path.equals("/api/video/feed") || path.equals("/api/video/list") || path.equals("/api/video/search")) {
            return true;
        }
        if (path.startsWith("/api/video/play/")) {
            return hasSinglePathSegment(path.substring("/api/video/play/".length()));
        }
        if (!path.startsWith("/api/video/")) {
            return false;
        }

        String rest = path.substring("/api/video/".length());
        if (hasSinglePathSegment(rest)) {
            return true;
        }
        return rest.endsWith("/comments") && hasSinglePathSegment(rest.substring(0, rest.length() - "/comments".length()));
    }

    private boolean hasSinglePathSegment(String value) {
        return value != null && !value.isBlank() && !value.contains("/");
    }
}
