package com.pat.product.exception;

import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.product.controller.ProductAdminController;
import com.pat.product.controller.ProductController;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
        ProductController.class,
        ProductAdminController.class
})
public class ProductExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "商品参数不合法" : error.getDefaultMessage())
                .orElse("商品参数不合法");
        return Result.error(ErrorCode.FARAMS_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage() == null ? "商品参数不合法" : violation.getMessage())
                .orElse("商品参数不合法");
        return Result.error(ErrorCode.FARAMS_ERROR, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error(ErrorCode.FARAMS_ERROR, "请求体格式错误");
    }
}
