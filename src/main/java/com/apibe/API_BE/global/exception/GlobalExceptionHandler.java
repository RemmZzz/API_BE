package com.apibe.API_BE.global.exception;

import com.apibe.API_BE.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

        return ResponseEntity.status(errorCode.getStatus().value()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ErrorCode.INVALID_REQUEST.getMessage())
                .errorCode(ErrorCode.INVALID_REQUEST.getCode())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus().value()).body(response);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ErrorCode.INVALID_REQUEST.getMessage())
                .errorCode(ErrorCode.INVALID_REQUEST.getCode())
                .errors(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus().value()).body(response);
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

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}

