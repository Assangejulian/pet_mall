package com.pat.demo.common.domain;

import lombok.Data;

/**
 * @author 13372
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private String description;

    public Result(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public Result() {

    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /** 无参成功响应（用于 void 操作） */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 错误码
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return error(errorCode.getCode(), message);
    }
    /**
     * 错误信息
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }


    public static Result error(int code, String message, String description) {
        return new Result(code, message, description);
    }
}
