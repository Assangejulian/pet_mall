//package com.artsail.common.config;
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
//    @Autowired
//    private RefreshTokenInterceptor refreshTokenInterceptor;
//    @Autowired
//    private LoginInterceptor loginInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        // token鍒锋柊鐨勬嫤鎴櫒
//        registry.addInterceptor(refreshTokenInterceptor)
//                .addPathPatterns("/**") // 鎷︽埅鎵€鏈夎矾寰?
//                .order(0);
//
//        // 鐧诲綍鎷︽埅鍣?
//        registry.addInterceptor(loginInterceptor)
//                .excludePathPatterns(
//                        "/user/code",
//                        "/user/login",
//                        "/shop/**",
//                        "/shop-type/**",
//                        "/upload/**",
//                        "/voucher/**",
//                        "/blog/hot")
//                .order(1); // 鎺掗櫎涓嶉渶瑕佺櫥褰曠殑璺緞
//    }
//}