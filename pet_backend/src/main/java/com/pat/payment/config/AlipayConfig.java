package com.pat.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类。
 *
 * <p>读取 application.yaml 中 alipay.* 配置项，初始化 AlipayClient Bean。
 * 沙箱网关：https://openapi.alipaydev.com/gateway.do
 * 正式网关：https://openapi.alipay.com/gateway.do</p>
 *
 * <pre>
 * alipay:
 *   app-id: ${ALIPAY_APPID}
 *   private-key: ${ALIPAY_PRIVATE_KEY}       # 商户私钥（PKCS8 格式）
 *   alipay-public-key: ${ALIPAY_PUBLIC_KEY}   # 支付宝公钥
 *   gateway: https://openapi.alipaydev.com/gateway.do
 *   notify-url: ${ALIPAY_NOTIFY_URL}          # 公网可访问的回调地址
 *   return-url: ${ALIPAY_RETURN_URL}          # 支付完成同步跳转
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /** 支付宝开放平台 AppId */
    private String appId;

    /** 商户应用私钥（PKCS8 格式，-----BEGIN PRIVATE KEY-----） */
    private String privateKey;

    /** 支付宝公钥（开放平台配置中获取） */
    private String alipayPublicKey;

    /** 网关地址：沙箱 https://openapi.alipaydev.com/gateway.do，正式 https://openapi.alipay.com/gateway.do */
    private String gateway = "https://openapi.alipaydev.com/gateway.do";

    /** 异步通知地址（公网可访问，用于接收支付结果回调） */
    private String notifyUrl;

    /** 支付完成同步跳转页面 */
    private String returnUrl;

    /**
     * 初始化 AlipayClient Bean。
     * 固定参数：json 格式、UTF-8 编码、RSA2 签名。
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(gateway, appId, privateKey,
                "json", "UTF-8", alipayPublicKey, "RSA2");
    }
}
