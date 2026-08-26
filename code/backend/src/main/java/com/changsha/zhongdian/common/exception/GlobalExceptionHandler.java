package com.changsha.zhongdian.common.exception;

import java.util.stream.Collectors;

import com.changsha.zhongdian.common.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::buildFieldErrorMessage)
                .collect(Collectors.joining("；"));

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unhandled exception caught by global handler", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("INTERNAL_SERVER_ERROR", "系统异常，请稍后重试"));
    }

    private String buildFieldErrorMessage(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage() == null ? "输入不合法" : fieldError.getDefaultMessage();
        return fieldError.getField() + "：" + defaultMessage;
    }
}
