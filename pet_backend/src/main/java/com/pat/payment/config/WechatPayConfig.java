package com.pat.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wx.miniapp")
public class WechatPayConfig {
    private String appid;
    private String secret;
    private String mchId;
    private String mchSerialNo;
    private String apiV3Key;
    private String privateKey;
    private String notifyUrl;
}
