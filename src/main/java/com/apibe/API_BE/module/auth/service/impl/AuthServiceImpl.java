package com.apibe.API_BE.module.auth.service.impl;

import com.apibe.API_BE.global.enums.UserRole;
import com.apibe.API_BE.global.enums.UserStatus;
import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.global.security.CustomUserDetails;
import com.apibe.API_BE.global.security.JwtTokenProvider;
import com.apibe.API_BE.infrastructure.email.EmailService;
import com.apibe.API_BE.module.auth.dto.request.*;
import com.apibe.API_BE.module.auth.dto.response.TokenResponse;
import com.apibe.API_BE.module.auth.dto.response.UserProfileResponse;
import com.apibe.API_BE.module.auth.mapper.AuthMapper;
import com.apibe.API_BE.module.auth.service.AuthService;
import com.apibe.API_BE.module.user.entity.OtpVerification;
import com.apibe.API_BE.module.user.entity.PasswordResetToken;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.entity.UserSession;
import com.apibe.API_BE.module.user.repository.OtpVerificationRepository;
import com.apibe.API_BE.module.user.repository.PasswordResetTokenRepository;
import com.apibe.API_BE.module.user.repository.UserRepository;
import com.apibe.API_BE.module.user.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Thêm 3 thư viện để xử lý băm chuỗi (Hash)
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String REGISTER_PURPOSE = "REGISTER";
    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L;

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final AuthMapper authMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend.reset-password-url:http://localhost:5173/reset-password}")
    private String resetPasswordUrl;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Password confirmation does not match");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .name(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .status(UserStatus.PENDING)
                .build();
        userRepository.save(user);

        String otp = generateOtp();
        OtpVerification otpVerification = OtpVerification.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .otpHash(passwordEncoder.encode(otp))
                .purpose(REGISTER_PURPOSE)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        otpVerificationRepository.save(otpVerification);
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {
        OtpVerification otpVerification = otpVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), REGISTER_PURPOSE)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "OTP not found"));

        if (otpVerification.getVerifiedAt() != null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "OTP already verified");
        }
        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "OTP has expired");
        }
        if (!passwordEncoder.matches(request.getOtp(), otpVerification.getOtpHash())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "OTP is invalid");
        }

        User user = userRepository.findById(otpVerification.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        otpVerification.setVerifiedAt(LocalDateTime.now());
        user.setStatus(UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Email or password is invalid"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.FORBIDDEN, "User account is not active");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Email or password is invalid");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        userSessionRepository.save(UserSession.builder()
                .userId(user.getId())
                .refreshTokenHash(hashToken(refreshToken)) // <--- SỬ DỤNG SHA-256 THAY CHO BCRYPT
                .ipAddress(resolveIpAddress(servletRequest))
                .userAgent(servletRequest.getHeader("User-Agent"))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());
        user.setLastLoginAt(LocalDateTime.now());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(ACCESS_TOKEN_EXPIRES_IN_SECONDS)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken()) || !jwtTokenProvider.isRefreshToken(request.getRefreshToken())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Refresh token is invalid");
        }

        UUID userId = jwtTokenProvider.getUserId(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Refresh token is invalid"));
        UserSession session = findActiveSessionByRefreshToken(userId, request.getRefreshToken());
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Session has expired");
        }

        return TokenResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(request.getRefreshToken())
                .expiresIn(ACCESS_TOKEN_EXPIRES_IN_SECONDS)
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            if (!jwtTokenProvider.validateToken(request.getRefreshToken()) || !jwtTokenProvider.isRefreshToken(request.getRefreshToken())) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Refresh token is invalid");
            }
            UserSession session = findActiveSessionByRefreshToken(jwtTokenProvider.getUserId(request.getRefreshToken()), request.getRefreshToken());
            session.setRevokedAt(now);
            return;
        }

        UUID currentUserId = getCurrentUser().getId();
        userSessionRepository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfter(currentUserId, now)
                .forEach(session -> session.setRevokedAt(now));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse me() {
        User user = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        return authMapper.toUserProfileResponse(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(passwordEncoder.encode(token))
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build());
        emailService.sendResetPasswordEmail(user.getEmail(), resetPasswordUrl + "?token=" + token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Password confirmation does not match");
        }
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByUsedAtIsNullAndExpiresAtAfter(LocalDateTime.now())
                .stream()
                .filter(candidate -> passwordEncoder.matches(request.getToken(), candidate.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Reset token is invalid or expired"));

        User user = userRepository.findById(passwordResetToken.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        passwordResetToken.setUsedAt(LocalDateTime.now());
    }

    private UserSession findActiveSessionByRefreshToken(UUID userId, String refreshToken) {
        List<UserSession> sessions = userSessionRepository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfter(userId, LocalDateTime.now());
        return sessions.stream()
                .filter(session -> hashToken(refreshToken).equals(session.getRefreshTokenHash())) // <--- ĐỐI CHIẾU BẰNG SHA-256
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Session not found"));
    }

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        return userDetails;
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // <--- HÀM MỚI THÊM ĐỂ BĂM TOKEN BẰNG SHA-256 --->
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Could not hash token", e);
        }
    }
}