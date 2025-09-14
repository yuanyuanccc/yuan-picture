package com.yuanc.yuanpicturebackend.exception;

import com.yuanc.yuanpicturebackend.common.BaseResponse;
import com.yuanc.yuanpicturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> bussinessExceptionHandle(BusinessException e) {
        log.error("bussinessException",e);
        return ResultUtils.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> RuntimeExceptionHandle(RuntimeException e) {
        log.error("RuntimeException",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"系统错误");
    }
}
