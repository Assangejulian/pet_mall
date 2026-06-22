//package com.artsail.common.interceptor;
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
//        // 1. 鍒ゆ柇鏄惁闇€瑕佹嫤鎴紙ThreadLocal涓槸鍚︽湁鐢ㄦ埛锛?
//        if (userHolder.getUser() == null) {
//            // 2. 娌℃湁锛岄渶瑕佹嫤鎴紝璁剧疆鐘舵€佺爜
//            response.setStatus(401);
//            // 3. 鎷︽埅
//            return false;
//        }
//        // 4. 鏈夌敤鎴凤紝鍒欐斁琛?
//        return true;
//    }
//}