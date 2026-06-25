package com.pat.user.service.auth;

import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;

/**
 * 认证策略接口 —— 每种登录方式一个实现
 */
public interface AuthService {
    /** 执行认证，返回 User 实体（含 id/role 用于生成 JWT） */
    User authenticate(LoginDTO dto);
}
