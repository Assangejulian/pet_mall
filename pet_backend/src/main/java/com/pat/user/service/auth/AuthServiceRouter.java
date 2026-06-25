package com.pat.user.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 认证策略路由器 —— 根据 authType 分发到对应的 AuthService
 * <p>Spring 自动注入 Map&lt;String, AuthService&gt;，key 为 bean name（如 passwordAuthService）</p>
 */
@Component
public class AuthServiceRouter {

    @Autowired
    private Map<String, AuthService> serviceMap;

    public AuthService getService(String authType) {
        AuthService svc = serviceMap.get(authType + "AuthService");
        if (svc == null) {
            throw new RuntimeException("不支持的认证方式: " + authType);
        }
        return svc;
    }
}