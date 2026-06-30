package com.pat.user.service.auth;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
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
        // fallback: 处理带后缀的认证类型 (wechat_pc → wechatAuthService)
        if (svc == null && authType.contains("_")) {
            String base = authType.substring(0, authType.indexOf("_"));
            svc = serviceMap.get(base + "AuthService");
        }
        if (svc == null) {
            throw new BusinessException(ErrorCode.AUTH_TYPE_UNSUPPORTED);
        }
        return svc;
    }
}