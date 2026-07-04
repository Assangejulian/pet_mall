package com.pat.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat-pay")
public class WechatPayConfig {
    private String appid;
    private String mchId;
    private String mchSerialNo;
    private String privateKey;
    private String apiV3Key;
    private String notifyUrl;
}