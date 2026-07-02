package com.pat.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.interceptor.AuthInterceptor;
import com.pat.common.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

/**
 * 拦截器注册配置。
 *
 * <p>设计原则：不声明拦截器 = 公开访问。
 *    需要保护的路径只注册一次，不搞双重 catch-all + exclude 的模式。
 *
 * <h3>权限分层</h3>
 * <pre>
 * /api/admin/**     → RoleInterceptor("admin")          管理员
 * /api/auditor/**   → RoleInterceptor("auditor","admin")  审核员(管理员也可)
 * /api/merchant/**  → RoleInterceptor("merchant")        商家
 * /api/order/**        → AuthInterceptor                   任意登录用户
 * /api/cart/**         → AuthInterceptor
 * /api/user/address/** → AuthInterceptor
 * /api/ai/**           → AuthInterceptor
 * /api/comment/**      → AuthInterceptor
 * /api/upload/**       → AuthInterceptor
 * /api/video/**        → AuthInterceptor，GET 读接口由拦截器内部放行
 * 其余 /api/**      → 无拦截器 = 游客可访问
 * </pre>
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    // ═══════════════════ 角色拦截器 Bean ═══════════════════

    @Bean
    RoleInterceptor adminRole() {
        return new RoleInterceptor(objectMapper, Set.of("admin"));
    }

    @Bean
    RoleInterceptor auditorRole() {
        // 管理员也能访问审核接口
        return new RoleInterceptor(objectMapper, Set.of("auditor", "admin"));
    }

    @Bean
    RoleInterceptor merchantRole() {
        return new RoleInterceptor(objectMapper, Set.of("merchant"));
    }

    // ═══════════════════ 注册 ═══════════════════

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ── 角色拦截（order=0，优先执行）──
        registry.addInterceptor(adminRole())
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login")  // 登录接口公开
                .order(0);

        registry.addInterceptor(auditorRole())
                .addPathPatterns("/api/auditor/**")
                .order(0);

        registry.addInterceptor(merchantRole())
                .addPathPatterns("/api/merchant/**")
                .order(0);

        // ── 认证拦截（order=1，需登录但不管角色）──
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                    "/api/order/**",
                    "/api/cart/**",
                    "/api/user/address/**",
                    "/api/ai/**",
                    "/api/comment/**",
                    "/api/upload/**",
                    "/api/video/**"
                )
                .order(1);

        // 未在此注册的 /api/** 路径一律公开，无需声明 exclude
    }
}
