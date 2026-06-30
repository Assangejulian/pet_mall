package com.pat.user.helper;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 微信登录共享逻辑：仅封装微信 API 调用
 */
@Slf4j
@Component
public class WechatHelper {

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
            log.error("微信 {} 接口失败，缺少{}，响应: {}", apiName, fieldName, resp);
            if (errCode != null) {
                throw new BusinessException(ErrorCode.WX_API_FAILED);
            }
            throw new BusinessException(ErrorCode.WX_API_FAILED);
        }
        return value;
    }
}
