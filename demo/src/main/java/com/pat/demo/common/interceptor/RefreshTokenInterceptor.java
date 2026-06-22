package com.pat.demo.common.interceptor;//package com.artsail.common.interceptor;
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
////在拦截器中设置用户信息
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
//        // 1. 获取token
//        String token = request.getHeader("authorization");
//        if (StrUtil.isBlank(token)) {
//            return true;
//        }
//
//        // 2. 基于token获取redis中的用户
//        String key = RedisConstants.LOGIN_USER_KEY + token;
//        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(key);
//
//        // 3. 判断用户是否存在
//        if (userMap.isEmpty()) {
//            return true;
//        }
//
//        // 4. 将查询到的hash数据转为UserDTO
//        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//
//        // 5. 存在，保存用户信息到ThreadLocal
//        userHolder.saveUser(userDTO);
//
//        // 6. 刷新token有效期
//        redisTemplate.expire(key, Duration.ofMinutes(RedisConstants.LOGIN_USER_TTL));
//
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        // 移除用户，防止内存泄漏
//        userHolder.removeUser();
//    }
//}