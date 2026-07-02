package com.pat.common.config;

import com.pat.common.interceptor.AdminAuthInterceptor;
import com.pat.common.interceptor.UserAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Autowired
    private UserAuthInterceptor userAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthInterceptor).addPathPatterns("/api/order/**", "/api/cart/**", "/api/user/address/**").order(1);

        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login")
                .order(0);

        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/user/login",
                    "/api/user/send-email-code",
                    "/api/user/send-sms-code",
                    "/api/admin/**",
                    "/api/product/**",
                    "/api/category/**",
                    "/api/store/**",
                    "/api/video/**",
                    "/api/comment/**",
                    "/api/upload/**",
                    "/api/notify/**"
                )
                .order(1);
    }
}
