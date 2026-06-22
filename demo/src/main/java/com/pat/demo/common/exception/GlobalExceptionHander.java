package com.pat.demo.common.exception;


import com.artsail.common.domain.ErrorCode;
import com.artsail.common.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHander {

    @ExceptionHandler(BusinessException.class)
    public Result  handleBusinessException(BusinessException e){
        log.warn("业务异常: {}", e.getMessage()); // 2. 去掉堆栈打印，减少日志噪音
        return Result.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e){
        log.error("系统运行时异常", e); // 3. 必须带上 e (堆栈信息)，方便排查
        return Result.error(ErrorCode.SYSTEM_ERROR,e.getMessage());
    }

}
