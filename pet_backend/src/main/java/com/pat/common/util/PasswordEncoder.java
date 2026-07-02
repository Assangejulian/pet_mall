package com.pat.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 简易密码编码器（无需 Spring Security 依赖）
 * 使用 SHA-256 + 随机盐
 */
public class PasswordEncoder {

    private static final int SALT_LENGTH = 16;

    /**
     * 加密密码：生成盐并计算哈希
     */
    public static String encode(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        byte[] hash = hashWithSalt(rawPassword, salt);
        // 格式: base64(salt) + ":" + base64(hash)
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 校验密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        String[] parts = encodedPassword.split(":");
        if (parts.length != 2) return false;
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = hashWithSalt(rawPassword, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private static byte[] hashWithSalt(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
