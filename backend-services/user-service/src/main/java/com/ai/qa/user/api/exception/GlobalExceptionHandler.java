package com.ai.qa.user.api.exception;


import com.ai.qa.user.api.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义的业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        ErrorCode errorCode = ex.getErrorCode();
        return new ResponseEntity<>(ApiResponse.failure(errorCode), errorCode.getHttpStatus());
    }

    /**
     * 处理JPA等持久化层抛出的“实体未找到”异常
     * 这是将基础设施层的异常转换为统一业务响应的好例子
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("实体未找到: {}", ex.getMessage());
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND; // 映射到一个具体的业务错误码
        return new ResponseEntity<>(ApiResponse.failure(errorCode, ex.getMessage()), errorCode.getHttpStatus());
    }

    /**
     * 处理所有未被捕获的异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllUncaughtException(Exception ex) {
        // 对于未知的严重异常，需要记录详细的错误日志
        log.error("发生未知异常", ex);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(ApiResponse.failure(errorCode), errorCode.getHttpStatus());
    }

    /**
     * 处理自定义的业务异常
     * 针对request数据进行参数校验
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(MethodArgumentNotValidException ex) {
        log.warn("request数据参数校验异常: {}", ex.getMessage());
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        return new ResponseEntity<>(ApiResponse.failure(errorCode, ex.getMessage()), errorCode.getHttpStatus());
    }
}