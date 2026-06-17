package com.apibe.API_BE.module.auth.service;

import com.apibe.API_BE.module.auth.dto.request.*;
import com.apibe.API_BE.module.auth.dto.response.TokenResponse;
import com.apibe.API_BE.module.auth.dto.response.UserProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    void register(RegisterRequest request);

    void verifyOtp(VerifyOtpRequest request);

    TokenResponse login(LoginRequest request, HttpServletRequest servletRequest);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    UserProfileResponse me();

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}

