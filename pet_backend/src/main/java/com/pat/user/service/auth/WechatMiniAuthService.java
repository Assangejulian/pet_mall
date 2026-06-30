package com.pat.user.service.auth;

import cn.hutool.json.JSONObject;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.helper.WechatHelper;
import com.pat.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 微信小程序登录
 * <p>路由：authType = wechat → wechatAuthService → WechatMiniAuthService</p>
 * 通过 wx.login() 获取 code，调用 code2session 换取 openid/unionid
 */
@Slf4j
@Service("wechatAuthService")
public class WechatMiniAuthService implements AuthService {

    @Autowired
    private WechatHelper wechatHelper;

    @Autowired
    private UserService userService;

    @Value("${wx.miniapp.appid}")
    private String appid;

    @Value("${wx.miniapp.secret}")
    private String secret;

    @Override
    public User authenticate(LoginDTO dto) {
        // 1. 校验并提取 wxCode
        // 2. 调微信 code2session 换取 openid/unionid → 查/建用户
        String wxCode = getWxCode(dto);
        return miniappLogin(wxCode);
    }

    /** 从 dto 中提取 wxCode，为空则抛异常 */
    private String getWxCode(LoginDTO dto) {
        String wxCode = dto.getWxCode();
        if (wxCode == null || wxCode.isBlank()) {
            throw new BusinessException(ErrorCode.WX_CODE_EMPTY);
        }
        return wxCode;
    }

    /** 小程序登录流程：code2session → 查/建用户 */
    public User miniappLogin(String code) {
        JSONObject session = code2session(code);
        String openid = wechatHelper.getRequiredResponse(session, "openid", "code2session");
        String unionid = session.getStr("unionid");
        return userService.findOrCreateByWechat(openid, unionid);
    }

    /** 调用微信 code2session 接口，换取 openid + session_key */
    private JSONObject code2session(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);
        return wechatHelper.callWxApi(url);
    }
}
