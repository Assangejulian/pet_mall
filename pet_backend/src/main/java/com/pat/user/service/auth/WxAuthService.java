package com.pat.user.service.auth;

import com.pat.user.entity.User;

/**
 * 微信登录服务接口（小程序 wx.login / PC 扫码）
 */
public interface WxAuthService {

    /**
     * 小程序端：code2session 登录
     * @param code wx.login 返回的临时 code
     * @return 登录/注册后的用户
     */
    User miniappLogin(String code);

    /**
     * PC 端：网页扫码回调登录
     * @param code 微信 OAuth2 回调 code
     * @return 登录/注册后的用户
     */
    User pcScanLogin(String code);
}
