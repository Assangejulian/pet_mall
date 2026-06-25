package com.pat.user.service.auth;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.user.dto.LoginDTO;
import com.pat.user.entity.User;
import com.pat.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * 微信登录
 * authType = wechat
 * 支持：小程序 wx.login（code2session）、PC 网页扫码（OAuth2）
 */
@Slf4j
@Service("wechatAuthService")
public class WechatAuthService implements AuthService, WxAuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("")
    private String miniappAppid;

    @Value("")
    private String miniappSecret;

    @Value("")
    private String pcAppid;

    @Value("")
    private String pcSecret;

    // ========== AuthService 策略入口（小程序端） ==========

    @Override
    public User authenticate(LoginDTO dto) {
        String wxCode = dto.getWxCode();
        if (wxCode == null || wxCode.isBlank()) {
            throw new RuntimeException("微信授权 code 不能为空");
        }
        return miniappLogin(wxCode);
    }

    // ========== 小程序：code2session ==========

    @Override
    public User miniappLogin(String code) {
        // 1. 调微信 code2session
        JSONObject session = code2session(miniappAppid, miniappSecret, code);
        String openid = session.getStr("openid");
        if (openid == null) {
            log.error("微信 code2session 失败: {}", session);
            throw new RuntimeException("微信登录失败，无法获取 openid");
        }
        String unionid = session.getStr("unionid");

        // 2. 查/建用户
        return findOrCreateUser(openid, unionid);
    }

    // ========== PC 网页扫码登录 ==========

    @Override
    public User pcScanLogin(String code) {
        // 1. 获取 access_token
        JSONObject tokenResp = getAccessToken(pcAppid, pcSecret, code);
        String accessToken = tokenResp.getStr("access_token");
        String openid = tokenResp.getStr("openid");
        if (accessToken == null || openid == null) {
            log.error("微信获取 access_token 失败: {}", tokenResp);
            throw new RuntimeException("微信登录失败");
        }

        // 2. 获取用户信息（昵称、头像等）
        JSONObject userInfo = getUserInfo(accessToken, openid);

        // 3. 查/建用户（含用户信息同步）
        String unionid = userInfo.getStr("unionid");
        User user = findOrCreateUser(openid, unionid);

        // 同步昵称和头像（仅在首次或信息变更时）
        if (user.getAvatar() == null || user.getAvatar().isBlank()) {
            user.setAvatar(userInfo.getStr("headimgurl"));
            user.setRealName(userInfo.getStr("nickname"));
            userMapper.updateById(user);
        }
        return user;
    }

    // ========== 微信 API 调用 ==========

    /** code2session（小程序） */
    private JSONObject code2session(String appid, String secret, String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);
        String resp = restTemplate.getForObject(url, String.class);
        return JSONUtil.parseObj(resp);
    }

    /** 获取 access_token（PC 扫码 OAuth2） */
    private JSONObject getAccessToken(String appid, String secret, String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appid, secret, code);
        String resp = restTemplate.getForObject(url, String.class);
        return JSONUtil.parseObj(resp);
    }

    /** 获取用户信息（PC 扫码） */
    private JSONObject getUserInfo(String accessToken, String openid) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                accessToken, openid);
        String resp = restTemplate.getForObject(url, String.class);
        return JSONUtil.parseObj(resp);
    }

    // ========== 用户查/建 ==========

    @Transactional
    public User findOrCreateUser(String openid, String unionid) {
        // 优先按 unionid 查，没有则按 openid 查
        User user = null;
        if (unionid != null && !unionid.isBlank()) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUnionid, unionid));
        }
        if (user == null) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        }
        if (user != null) {
            // 更新 unionid（如果之前没有）
            if (unionid != null && !unionid.isBlank() && user.getUnionid() == null) {
                user.setUnionid(unionid);
                userMapper.updateById(user);
            }
            return user;
        }

        // 新建用户
        user = new User();
        user.setUsername("wx_" + openid.substring(0, 8));
        user.setPassword("");
        user.setOpenid(openid);
        user.setUnionid(unionid);
        user.setRole("user");
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }
}
