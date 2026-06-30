package com.pat.common.domain;

import lombok.Getter;

/**
 * 错误码
 *
 * @author 13372
 */
@Getter
public enum ErrorCode {
    FARAMS_ERROR(408, "参数格式错误", ""),
    FARAMS_NULL_ERROR(409, "参数不能为空", ""),
    NOT_AUTH(401, "未授权", ""),
    SYSTEM_ERROR(500, "系统内部错误", ""),
    NOT_FOUND(404, "资源未找到", ""),
    // ── 用户认证 ──
    USER_NOT_FOUND(404, "用户不存在", ""),
    USER_DISABLED(403, "账号已禁用", ""),
    PASSWORD_WRONG(401, "密码错误", ""),
    USERNAME_PHONE_EMPTY(400, "用户名或手机号不能为空", ""),
    // ── 验证码 ──
    CODE_EMPTY(400, "验证码不能为空", ""),
    CODE_WRONG(401, "验证码错误或已过期", ""),
    // ── 微信 ──
    WX_CODE_EMPTY(400, "微信授权code不能为空", ""),
    WX_API_FAILED(502, "微信接口调用失败", ""),
    // ── 人脸 ──
    FACE_IMAGE_EMPTY(400, "人脸识别失败，未收到图片", ""),
    FACE_DETECT_FAILED(501, "未检测到人脸", ""),
    // ── 路由 ──
    AUTH_TYPE_UNSUPPORTED(400, "不支持的认证方式", ""),
    SAVE_FAILED(500, "保存失败", ""),
    UPDATE_FAILED(500, "更新失败", ""),
    DELETE_FAILED(500, "删除失败", "");

    private final int code;
    private final String message;
    private final String description;

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
