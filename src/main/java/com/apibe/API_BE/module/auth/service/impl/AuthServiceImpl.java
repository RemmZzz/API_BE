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
import com.apibe.API_BE.module.user.entity.Oauth2ExchangeCode;
import com.apibe.API_BE.module.user.entity.OtpVerification;
import com.apibe.API_BE.module.user.entity.PasswordResetToken;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.entity.UserSession;
import com.apibe.API_BE.module.user.repository.Oauth2ExchangeCodeRepository;
import com.apibe.API_BE.module.user.repository.OtpVerificationRepository;
import com.apibe.API_BE.module.user.repository.PasswordResetTokenRepository;
import com.apibe.API_BE.module.user.repository.UserRepository;
import com.apibe.API_BE.module.user.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@SuppressWarnings("null")
public class AuthServiceImpl implements AuthService {

    private static final String REGISTER_PURPOSE = "REGISTER";
    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L;

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Oauth2ExchangeCodeRepository oauth2ExchangeCodeRepository;
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
                .otpHash(hashToken(otp)) // <--- SHA-256 băm nhanh, tránh CPU DoS
                .purpose(REGISTER_PURPOSE)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .failedAttempts(0)
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
        if (otpVerification.getFailedAttempts() >= 5) {
            throw new AppException(ErrorCode.BAD_REQUEST, "OTP đã bị vô hiệu hóa do nhập sai quá 5 lần");
        }

        if (!hashToken(request.getOtp()).equals(otpVerification.getOtpHash())) {
            int newFailedAttempts = otpVerification.getFailedAttempts() + 1;
            otpVerification.setFailedAttempts(newFailedAttempts);
            if (newFailedAttempts >= 5) {
                // Vô hiệu hóa OTP bằng cách đánh dấu hết hạn ngay lập tức
                otpVerification.setExpiresAt(LocalDateTime.now().minusSeconds(1));
            }
            otpVerificationRepository.save(otpVerification);
            
            if (newFailedAttempts >= 5) {
                throw new AppException(ErrorCode.BAD_REQUEST, "OTP đã bị vô hiệu hóa do nhập sai quá 5 lần");
            }
            throw new AppException(ErrorCode.BAD_REQUEST, "OTP is invalid");
        }

        User user = userRepository.findById(otpVerification.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        otpVerification.setVerifiedAt(LocalDateTime.now());
        user.setStatus(UserStatus.ACTIVE);
        otpVerificationRepository.save(otpVerification);
        userRepository.save(user);
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
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            // Tránh User Enumeration: không ném ngoại lệ nếu email không tồn tại, chỉ log nội bộ và trả về bình thường
            log.info("Quên mật khẩu: Email không tồn tại: {}", request.getEmail());
            return;
        }
        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(token)) // <--- SHA-256 thay vì BCrypt
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
        
        // Truy vấn trực tiếp từ database bằng SHA-256 hash của token để tránh quét toàn bộ DB và băm chậm BCrypt
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(hashToken(request.getToken()), LocalDateTime.now())
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Reset token is invalid or expired"));

        User user = userRepository.findById(passwordResetToken.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        passwordResetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(passwordResetToken);
        userRepository.save(user);
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

    @Override
    @Transactional
    public TokenResponse oauthExchange(Oauth2ExchangeRequest request, HttpServletRequest servletRequest) {
        Oauth2ExchangeCode exchangeCode = oauth2ExchangeCodeRepository.findByCode(request.getCode())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Invalid exchange code"));

        if (exchangeCode.getUsedAt() != null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Exchange code already used");
        }
        if (exchangeCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Exchange code has expired");
        }

        User user = userRepository.findById(exchangeCode.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        exchangeCode.setUsedAt(LocalDateTime.now());
        oauth2ExchangeCodeRepository.save(exchangeCode);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        userSessionRepository.save(UserSession.builder()
                .userId(user.getId())
                .refreshTokenHash(hashToken(refreshToken))
                .ipAddress(resolveIpAddress(servletRequest))
                .userAgent(servletRequest.getHeader("User-Agent"))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(ACCESS_TOKEN_EXPIRES_IN_SECONDS)
                .build();
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