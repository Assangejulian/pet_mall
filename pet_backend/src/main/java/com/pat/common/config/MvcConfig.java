package com.pat.common.config;

import com.pat.user.interceptor.AdminAuthInterceptor;
import com.pat.user.interceptor.UserAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Autowired
    private com.pat.common.interceptor.UserAuthInterceptor userAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserAuthInterceptor()).addPathPatterns("/api/order/**", "/api/cart/**", "/api/user/address/**").order(1);

        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login")
                .order(0);

        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/user/login",
                    "/api/admin/**",
                    "/api/product/**",
                    "/api/category/**",
                    "/api/common/**" // assuming some public endpoints
                )
                .order(1);
    }
}
