package com.apibe.API_BE.global.exception;

import com.apibe.API_BE.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(errorCode.getCode())
                .errors(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Internal server error")
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .errors(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(500).body(response);
    }
}

