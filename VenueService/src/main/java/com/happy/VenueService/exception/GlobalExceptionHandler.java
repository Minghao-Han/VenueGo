package com.happy.VenueService.exception;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.happy.VenueService.common.response.ApiResponse;
import com.happy.VenueService.config.EnvironmentConfig;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String INTERNAL_ERROR = "Internal server error";

    private final EnvironmentConfig environmentConfig;

    public GlobalExceptionHandler(EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ApiResponse(errors));
    }
    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedException(InterruptedException e) {
        return ResponseEntity.status(500).body("Thread interrupted");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleOther(Exception ex) {
        String message = ex.getMessage() == null || ex.getMessage().isBlank()
                ? INTERNAL_ERROR
                : ex.getMessage();
        if (environmentConfig.isProd()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(INTERNAL_ERROR));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(message));
    }

}
