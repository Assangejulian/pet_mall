package com.pat.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.pat.common.domain.Result;
import com.pat.common.util.JwtUtil;
import com.pat.common.util.UserHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 角色拦截器 —— 验证登录 + 校验角色。
 *
 * <p>流程：提取 Bearer token → 解析 JWT → 取 role 字段 → 查是否在 allowedRoles 中</p>
 * <p>allowedRoles 由 MvcConfig 在创建 Bean 时注入：
 *    <pre>
 *    new RoleInterceptor(objectMapper, Set.of("admin"))
 *    new RoleInterceptor(objectMapper, Set.of("auditor", "admin"))
 *    </pre>
 * </p>
 *
 * @see com.pat.common.config.MvcConfig
 * @see AuthInterceptor
 */
@Slf4j
public class RoleInterceptor implements HandlerInterceptor {

    private static final String BEARER = "Bearer ";

    private final ObjectMapper objectMapper;
    private final Set<String> allowedRoles;

    public RoleInterceptor(ObjectMapper objectMapper, Set<String> allowedRoles) {
        this.objectMapper = objectMapper;
        this.allowedRoles = Set.copyOf(allowedRoles);
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;

        // 1. 提取 token
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith(BEARER)) {
            writeJson(res, 401, "未登录或token无效");
            return false;
        }

        // 2. 解析 JWT
        Claims claims;
        try {
            claims = JwtUtil.parseToken(auth.substring(BEARER.length()));
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.warn("token校验失败: {}", e.getMessage());
            writeJson(res, 401, "token已过期或无效");
            return false;
        }

        // 3. 校验角色
        String role = claims.get("role", String.class);
        if (role == null || !allowedRoles.contains(role)) {
            writeJson(res, 403, "无访问权限");
            return false;
        }

        // 4. 存入上下文
        UserHolder.save("userId", claims.get("userId", Long.class));
        UserHolder.save("username", claims.get("username", String.class));
        UserHolder.save("role", role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        UserHolder.remove();
    }

    private void writeJson(HttpServletResponse res, int code, String msg) throws Exception {
        res.setStatus(code);
        res.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(res.getWriter(), Result.error(code, msg));
    }
}