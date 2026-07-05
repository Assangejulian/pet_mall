package com.pat.payment.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String privateKey;
    private String privateKeyPath;
    private String alipayPublicKey;
    private String alipayPublicKeyPath;
    private String gateway;
    private String notifyUrl;
    private String returnUrl;
    private String signType = "RSA2";
    private String charset = "utf-8";
    private String format = "json";

    @PostConstruct
    public void loadKeys() {
        if (privateKeyPath != null && !privateKeyPath.isBlank()) {
            try {
                privateKey = Files.readString(new ClassPathResource(privateKeyPath).getFile().toPath()).trim();
                log.info("Alipay private key loaded from {}, length={}", privateKeyPath, privateKey.length());
            } catch (Exception e) {
                log.error("Failed to load Alipay private key from {}", privateKeyPath, e);
            }
        }
        if (alipayPublicKeyPath != null && !alipayPublicKeyPath.isBlank()) {
            try {
                alipayPublicKey = Files.readString(new ClassPathResource(alipayPublicKeyPath).getFile().toPath()).trim();
                log.info("Alipay public key loaded from {}, length={}", alipayPublicKeyPath, alipayPublicKey.length());
            } catch (Exception e) {
                log.error("Failed to load Alipay public key from {}", alipayPublicKeyPath, e);
            }
        }
    }
}