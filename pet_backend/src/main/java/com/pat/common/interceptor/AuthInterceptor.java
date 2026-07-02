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

/**
 * 认证拦截器 —— 只验证用户是否登录，不校验角色。
 *
 * <p>流程：提取 Authorization 头中的 Bearer token → 解析 JWT → 存入 UserHolder</p>
 * <p>哪些路径需要登录由 MvcConfig 通过 addPathPatterns 控制。
 *    视频 Feed、详情、播放和评论列表是公开 GET，在这里做方法级放行。</p>
 *
 * @see com.pat.common.config.MvcConfig
 * @see RoleInterceptor
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER = "Bearer ";

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        if (isPublicVideoRead(req)) return true;

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith(BEARER)) {
            writeJson(res, 401, "未登录或token无效");
            return false;
        }

        Claims claims;
        try {
            claims = JwtUtil.parseToken(auth.substring(BEARER.length()));
        } catch (Exception e) {
            writeJson(res, 401, "token已过期或无效");
            return false;
        }

        // 存入当前线程，后续 Controller/Service 通过 UserHolder 获取
        UserHolder.save("userId", claims.get("userId", Long.class));
        UserHolder.save("username", claims.get("username", String.class));
        UserHolder.save("role", claims.get("role", String.class));
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

    private boolean isPublicVideoRead(HttpServletRequest req) {
        if (!"GET".equalsIgnoreCase(req.getMethod())) {
            return false;
        }

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
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
