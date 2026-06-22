//package com.artsail.common.interceptor;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.util.StrUtil;
//import com.email_eroll.DTO.UserDTO;
//import com.email_eroll.constant.RedisConstants;
//import com.email_eroll.until.UserHolder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.time.Duration;
//import java.util.Map;
////鍦ㄦ嫤鎴櫒涓缃敤鎴蜂俊鎭?
//@Component
//public class RefreshTokenInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Autowired
//    private UserHolder userHolder;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        // 1. 鑾峰彇token
//        String token = request.getHeader("authorization");
//        if (StrUtil.isBlank(token)) {
//            return true;
//        }
//
//        // 2. 鍩轰簬token鑾峰彇redis涓殑鐢ㄦ埛
//        String key = RedisConstants.LOGIN_USER_KEY + token;
//        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(key);
//
//        // 3. 鍒ゆ柇鐢ㄦ埛鏄惁瀛樺湪
//        if (userMap.isEmpty()) {
//            return true;
//        }
//
//        // 4. 灏嗘煡璇㈠埌鐨刪ash鏁版嵁杞负UserDTO
//        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//
//        // 5. 瀛樺湪锛屼繚瀛樼敤鎴蜂俊鎭埌ThreadLocal
//        userHolder.saveUser(userDTO);
//
//        // 6. 鍒锋柊token鏈夋晥鏈?
//        redisTemplate.expire(key, Duration.ofMinutes(RedisConstants.LOGIN_USER_TTL));
//
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        // 绉婚櫎鐢ㄦ埛锛岄槻姝㈠唴瀛樻硠婕?
//        userHolder.removeUser();
//    }
//}