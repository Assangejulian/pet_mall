package com.pat.demo.common.interceptor;//package com.artsail.common.interceptor;
//
//import com.email_eroll.until.UserHolder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//@Component
//public class LoginInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private UserHolder userHolder;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        // 1. 判断是否需要拦截（ThreadLocal中是否有用户）
//        if (userHolder.getUser() == null) {
//            // 2. 没有，需要拦截，设置状态码
//            response.setStatus(401);
//            // 3. 拦截
//            return false;
//        }
//        // 4. 有用户，则放行
//        return true;
//    }
//}