package com.pat.demo.common.domain;

import lombok.Getter;

/**
 * 错误码
 *
 * @author 13372
 */
@Getter
public enum ErrorCode {
    FARAMS_ERROR (408,"请求参数错误",""),
    FARAMS_NULL_ERROR (409,"请求参数为空",""),
    Not_AUTH(401,"无权限",""),
    SYSTEM_ERROR(500,"系统内部错误",""),
    NOT_FOUND(404, "资源未找到", ""),
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
