package com.apibe.API_BE.global.security;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.UUID;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        UUID reflectedId = extractId(principal);
        if (reflectedId != null) {
            return reflectedId;
        }

        String name = authentication.getName();
        try {
            return UUID.fromString(name);
        } catch (RuntimeException ex) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Cannot resolve current user id");
        }
    }

    private static UUID extractId(Object principal) {
        for (String methodName : new String[]{"getId", "getUserId"}) {
            try {
                Method method = principal.getClass().getMethod(methodName);
                Object value = method.invoke(principal);
                if (value instanceof UUID uuid) {
                    return uuid;
                }
                if (value instanceof String text) {
                    return UUID.fromString(text);
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Fallback to Authentication#getName.
            }
        }
        return null;
    }
}

