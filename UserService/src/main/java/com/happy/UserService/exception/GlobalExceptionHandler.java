package com.happy.UserService.exception;

import com.happy.UserService.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常，如"用户不存在"
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getCode())
                .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    // 兜底，未预期的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail(500, "Internal server error"));
    }
}