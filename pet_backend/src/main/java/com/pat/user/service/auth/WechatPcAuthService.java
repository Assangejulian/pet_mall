package com.pat.user.service.auth;

import cn.hutool.json.JSONObject;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 微信 PC 网页扫码登录
 * <p>路由：authType = wechat_pc → wechat_pcAuthService → WechatPcAuthService</p>
 * 通过微信 OAuth2 扫码回调获取 code，换取 access_token + 用户信息
 */
@Slf4j
@Service("wechat_pcAuthService")
public class WechatPcAuthService implements AuthService {

    @Autowired
    private WechatHelper wechatHelper;

    @Value("${wx.pc.appid}")
    private String appid;

    @Value("${wx.pc.secret}")
    private String secret;

    @Override
    public User authenticate(LoginDTO dto) {
        // 1. 校验并提取 wxCode
        // 2. OAuth2 流程：access_token → 用户信息 → 查/建用户 → 同步头像昵称
        String wxCode = getWxCode(dto);
        return pcScanLogin(wxCode);
    }

    /** 从 dto 中提取 wxCode，为空则抛异常 */
    private String getWxCode(LoginDTO dto) {
        String wxCode = dto.getWxCode();
        if (wxCode == null || wxCode.isBlank()) {
            throw new RuntimeException("微信授权 code 不能为空");
        }
        return wxCode;
    }

    /** PC 扫码登录流程：getAccessToken → getUserInfo → 查/建用户 → 同步头像昵称 */
    public User pcScanLogin(String code) {
        JSONObject tokenResp = getAccessToken(code);
        String accessToken = wechatHelper.getRequiredResponse(tokenResp, "access_token", "access_token");
        String openid = wechatHelper.getRequiredResponse(tokenResp, "openid", "access_token");

        JSONObject userInfo = getUserInfo(accessToken, openid);
        User user = wechatHelper.findOrCreateUser(openid, userInfo.getStr("unionid"));
        syncWechatUserInfo(user, userInfo);
        return user;
    }

    /** 第一步：用 code 换取 access_token + openid */
    private JSONObject getAccessToken(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appid, secret, code);
        return wechatHelper.callWxApi(url);
    }

    /** 第二步：用 access_token 获取微信用户信息（昵称、头像） */
    private JSONObject getUserInfo(String accessToken, String openid) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                accessToken, openid);
        return wechatHelper.callWxApi(url);
    }

    /** 第三步：同步微信昵称和头像到本地用户（仅在首次） */
    private void syncWechatUserInfo(User user, JSONObject userInfo) {
        if (user.getAvatar() == null || user.getAvatar().isBlank()) {
            user.setAvatar(userInfo.getStr("headimgurl"));
            user.setRealName(userInfo.getStr("nickname"));
            wechatHelper.updateUserById(user);
        }
    }
}
