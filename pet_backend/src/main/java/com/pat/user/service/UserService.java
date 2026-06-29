package com.pat.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.user.domain.entity.User;

public interface UserService extends IService<User> {
    /** 创建新用户，自动填充默认值（password="" / role="user" / status=1） */
    User createUser(User user);

    /**
     * 检查用户状态，用户不存在或已禁用时抛 BusinessException
     */
    void checkUserActive(User user);
}
