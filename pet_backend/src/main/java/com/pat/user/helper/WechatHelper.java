package com.pat.user.helper;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 微信登录共享逻辑，被 WechatMiniAuthService / WechatPcAuthService 共同注入使用。
 * <ul>
 *   <li>callWxApi / getRequiredResponse — 微信 API 调用与响应解析</li>
 *   <li>findOrCreateUser / updateUserById — 用户查/建/更新</li>
 * </ul>
 */
@Slf4j
@Component
public class WechatHelper {

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    /** 通用 GET 请求调用微信 API，返回 JSON 响应 */
    public JSONObject callWxApi(String url) {
        String resp = restTemplate.getForObject(url, String.class);
        return JSONUtil.parseObj(resp);
    }

    /** 从微信响应中提取字段，为空则根据 errcode 抛异常 */
    public String getRequiredResponse(JSONObject resp, String fieldName, String apiName) {
        String value = resp.getStr(fieldName);
        if (value == null) {
            Integer errCode = resp.getInt("errcode");
            String errMsg = resp.getStr("errmsg");
            log.error("微信 {} 接口失败，缺少 {}，响应: {}", apiName, fieldName, resp);
            if (errCode != null) {
                throw new BusinessException(ErrorCode.WX_API_FAILED);
            }
            throw new BusinessException(ErrorCode.WX_API_FAILED);
        }
        return value;
    }

    /** 按 unionid/openid 查用户，不存在则自动创建 */
    public User findOrCreateUser(String openid, String unionid) {
        User user = findUserByWechat(openid, unionid);
        if (user != null) {
            syncUnionid(user, unionid);
            return user;
        }
        return createWechatUser(openid, unionid);
    }

    private User findUserByWechat(String openid, String unionid) {
        if (unionid != null && !unionid.isBlank()) {
            User user = userService.getOne(
                    new LambdaQueryWrapper<User>().eq(User::getUnionid, unionid));
            if (user != null) return user;
        }
        return userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }

    private void syncUnionid(User user, String unionid) {
        if (unionid != null && !unionid.isBlank() && user.getUnionid() == null) {
            user.setUnionid(unionid);
            userService.updateById(user);
        }
    }

    private User createWechatUser(String openid, String unionid) {
        User user = new User();
        user.setUsername("wx_" + openid.substring(0, 8));
        user.setOpenid(openid);
        user.setUnionid(unionid);
        userService.createUser(user);
        return user;
    }

    /** 更新用户信息（用于同步微信昵称/头像） */
    public void updateUserById(User user) {
        userService.updateById(user);
    }
}
