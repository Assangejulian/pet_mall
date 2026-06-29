package com.pat.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.Result;
import com.pat.common.utils.JwtUtil;
import com.pat.common.utils.UserHolder;
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
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 检查 Authorization 头
        String auth = request.getHeader(AUTH_HEADER);
        if (auth == null || !auth.startsWith(BEARER_PREFIX)) {
            writeJson(response, 401, "未登录或token无效");
            return false;
        }

        // 2. 解析 Token（校验+解析一步完成，避免双重解析）
        String token = auth.substring(BEARER_PREFIX.length());
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            writeJson(response, 401, "token已过期或无效");
            return false;
        }

        // 3. 注入用户上下文
        UserHolder.save(KEY_USER_ID, claims.get(KEY_USER_ID, Long.class));
        UserHolder.save(KEY_USERNAME, claims.get(KEY_USERNAME, String.class));
        UserHolder.save(KEY_ROLE, claims.get(KEY_ROLE, String.class));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.remove();
    }

    /** 写入统一格式的错误响应 */
    private void writeJson(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), Result.error(code, message));
    }
}
