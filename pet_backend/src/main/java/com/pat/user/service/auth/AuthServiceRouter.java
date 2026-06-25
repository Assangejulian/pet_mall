package com.pat.user.service.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证策略路由器 —— 根据 authType 分发到对应的 AuthService
 */
@Component
public class AuthServiceRouter {

    private final Map<String, AuthService> serviceMap = new HashMap<>();

    @Autowired
    private List<AuthService> authServiceList;

    @PostConstruct
    public void init() {
        for (AuthService svc : authServiceList) {
            // 从 Spring bean name 中提取 authType
            // 约定 bean name = {authType}AuthService，如 passwordAuthService
            String beanName = svc.getClass().getSimpleName();
            // PasswordAuthService → password
            String authType = Character.toLowerCase(beanName.charAt(0))
                    + beanName.substring(1).replace("AuthService", "");
            serviceMap.put(authType, svc);
        }
    }

    public AuthService getService(String authType) {
        AuthService svc = serviceMap.get(authType);
        if (svc == null) {
            throw new RuntimeException("不支持的认证方式: " + authType);
        }
        return svc;
    }
}
