package com.apibe.API_BE.global.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private List<String> errors;
    private LocalDateTime timestamp;
}

