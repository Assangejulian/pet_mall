package com.pat.user.service.auth;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * 寰俊鐧诲綍
 * authType = wechat / wechat_pc
 * 鏀寔锛氬皬绋嬪簭 wx.login锛坈ode2session锛夈€丳C 缃戦〉鎵爜锛圤Auth2锛?
 */
@Slf4j
@Service("wechatAuthService")
public class WechatAuthService implements AuthService, WxAuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${wx.miniapp.appid}")
    private String miniappAppid;

    @Value("${wx.miniapp.secret}")
    private String miniappSecret;

    @Value("${wx.pc.appid}")
    private String pcAppid;

    @Value("${wx.pc.secret}")
    private String pcSecret;

    // ========== AuthService 策略入口（wechat / wechat_pc） ==========

    @Override
    public User authenticate(LoginDTO dto) {
        String wxCode = dto.getWxCode();
        if (wxCode == null || wxCode.isBlank()) {
            throw new RuntimeException("寰俊鎺堟潈 code 涓嶈兘涓虹┖");
        }
        if ("wechat_pc".equals(dto.getAuthType())) {
            return pcScanLogin(wxCode);
        }
        return miniappLogin(wxCode);
    }

    // ========== 灏忕▼搴忥細code2session ==========

    @Override
    public User miniappLogin(String code) {
        // 1. 璋冨井淇?code2session
        JSONObject session = code2session(miniappAppid, miniappSecret, code);
        String openid = session.getStr("openid");
        if (openid == null) {
            log.error("寰俊 code2session 澶辫触: {}", session);
            throw new RuntimeException("寰俊鐧诲綍澶辫触锛屾棤娉曡幏鍙?openid");
        }
        String unionid = session.getStr("unionid");

        // 2. 鏌?寤虹敤鎴?
        return findOrCreateUser(openid, unionid);
    }

    // ========== PC 缃戦〉鎵爜鐧诲綍 ==========

    @Override
    public User pcScanLogin(String code) {
        // 1. 鑾峰彇 access_token
        JSONObject tokenResp = getAccessToken(pcAppid, pcSecret, code);
        String accessToken = tokenResp.getStr("access_token");
        String openid = tokenResp.getStr("openid");
        if (accessToken == null || openid == null) {
            log.error("寰俊鑾峰彇 access_token 澶辫触: {}", tokenResp);
            throw new RuntimeException("寰俊鐧诲綍澶辫触");
        }

        // 2. 鑾峰彇鐢ㄦ埛淇℃伅锛堟樀绉般€佸ご鍍忕瓑锛?
        JSONObject userInfo = getUserInfo(accessToken, openid);

        // 3. 鏌?寤虹敤鎴凤紙鍚敤鎴蜂俊鎭悓姝ワ級
        String unionid = userInfo.getStr("unionid");
        User user = findOrCreateUser(openid, unionid);

        // 鍚屾鏄电О鍜屽ご鍍忥紙浠呭湪棣栨鎴栦俊鎭彉鏇存椂锛?
        if (user.getAvatar() == null || user.getAvatar().isBlank()) {
            user.setAvatar(userInfo.getStr("headimgurl"));
            user.setRealName(userInfo.getStr("nickname"));
            userMapper.updateById(user);
        }
        return user;
    }

    // ========== 寰俊 API 璋冪敤 ==========

    /** code2session锛堝皬绋嬪簭锛?*/
    private JSONObject code2session(String appid, String secret, String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);
        String resp = restTemplate.getForObject(url, String.class);
        return JSONUtil.parseObj(resp);
    }

    /** 鑾峰彇 access_token锛圥C 鎵爜 OAuth2锛?*/
    private JSONObject getAccessToken(String appid, String secret, String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appid, secret, code);
        String resp = restTemplate.getForObject(url, String.class);
        return JSONUtil.parseObj(resp);
    }

    /** 鑾峰彇鐢ㄦ埛淇℃伅锛圥C 鎵爜锛?*/
    private JSONObject getUserInfo(String accessToken, String openid) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                accessToken, openid);
        String resp = restTemplate.getForObject(url, String.class);
        return JSONUtil.parseObj(resp);
    }

    // ========== 鐢ㄦ埛鏌?寤?==========

    @Transactional
    public User findOrCreateUser(String openid, String unionid) {
        // 浼樺厛鎸?unionid 鏌ワ紝娌℃湁鍒欐寜 openid 鏌?
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
            // 鏇存柊 unionid锛堝鏋滀箣鍓嶆病鏈夛級
            if (unionid != null && !unionid.isBlank() && user.getUnionid() == null) {
                user.setUnionid(unionid);
                userMapper.updateById(user);
            }
            return user;
        }

        // 鏂板缓鐢ㄦ埛
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
