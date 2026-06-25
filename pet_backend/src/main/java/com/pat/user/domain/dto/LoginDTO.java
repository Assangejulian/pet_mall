package com.pat.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class LoginDTO {
    private String username;
    private String password;

    /** 认证类型: password / sms / email_code / wechat / face */
    @NotBlank
    private String authType;

    /** 手机号（sms / wechat 方式使用） */
    private String phone;

    /** 验证码（sms / email_code 方式使用） */
    private String code;

    /** 验证码 key（可选，用于校验） */
    private String codeKey;

    /** 微信临时登录 code（wechat 方式使用） */
    private String wxCode;

    /** 邮箱（email_code 方式使用） */
    private String email;

    /** 人脸登录标识（face 方式使用） */
    private String faceToken;

    /** 扩展字段，用于未来新增认证方式 */
    private Map<String, Object> extra = new HashMap<>();
}
