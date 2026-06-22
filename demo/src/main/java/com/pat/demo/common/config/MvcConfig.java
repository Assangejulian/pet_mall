package com.pat.demo.common.config;//package com.artsail.common.config;
//
//import com.email_eroll.interceptor.LoginInterceptor;
//import com.email_eroll.interceptor.RefreshTokenInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class MvcConfig implements WebMvcConfigurer {
//        @Autowired
//        private RefreshTokenInterceptor refreshTokenInterceptor;
//        @Autowired
//        private LoginInterceptor loginInterceptor;
//
//        @Override
//        public void addInterceptors(InterceptorRegistry registry) {
//                // token刷新的拦截器
//                registry.addInterceptor(refreshTokenInterceptor)
//                                .addPathPatterns("/**") // 拦截所有路径
//                                .order(0);
//
//                // 登录拦截器
//                registry.addInterceptor(loginInterceptor)
//                                .excludePathPatterns(
//                                                "/user/code",
//                                                "/user/login",
//                                                "/shop/**",
//                                                "/shop-type/**",
//                                                "/upload/**",
//                                                "/voucher/**",
//                                                "/blog/hot")
//                                .order(1); // 排除不需要登录的路径
//        }
//}