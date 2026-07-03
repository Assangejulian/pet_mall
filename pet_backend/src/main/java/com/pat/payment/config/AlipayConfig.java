package com.pat.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String gateway;
    private String notifyUrl;
    private String returnUrl;
    private String signType = "RSA2";
    private String charset = "utf-8";
    private String format = "json";
}
