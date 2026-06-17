package com.apibe.API_BE.auth;

import com.apibe.API_BE.global.enums.UserRole;
import com.apibe.API_BE.global.enums.UserStatus;
import com.apibe.API_BE.global.security.JwtTokenProvider;
import com.apibe.API_BE.infrastructure.email.EmailService;
import com.apibe.API_BE.module.auth.dto.request.LoginRequest;
import com.apibe.API_BE.module.auth.dto.request.RegisterRequest;
import com.apibe.API_BE.module.auth.dto.response.TokenResponse;
import com.apibe.API_BE.module.auth.mapper.AuthMapper;
import com.apibe.API_BE.module.auth.service.impl.AuthServiceImpl;
import com.apibe.API_BE.module.user.entity.OtpVerification;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.entity.UserSession;
import com.apibe.API_BE.module.user.repository.OtpVerificationRepository;
import com.apibe.API_BE.module.user.repository.PasswordResetTokenRepository;
import com.apibe.API_BE.module.user.repository.UserRepository;
import com.apibe.API_BE.module.user.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpVerificationRepository otpVerificationRepository;
    @Mock
    private UserSessionRepository userSessionRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private EmailService emailService;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerCreatesPendingUserAndOtp() {
        RegisterRequest request = RegisterRequest.builder()
                .username("minh")
                .email("minh@gmail.com")
                .password("12345678")
                .confirmPassword("12345678")
                .build();
        when(userRepository.existsByUsername("minh")).thenReturn(false);
        when(userRepository.existsByEmail("minh@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<OtpVerification> otpCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(userRepository).save(userCaptor.capture());
        verify(otpVerificationRepository).save(otpCaptor.capture());
        verify(emailService).sendOtpEmail(eq("minh@gmail.com"), matches("\\d{6}"));

        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.USER);
        assertThat(otpCaptor.getValue().getPurpose()).isEqualTo("REGISTER");
        assertThat(otpCaptor.getValue().getEmail()).isEqualTo("minh@gmail.com");
    }

    @Test
    void loginReturnsTokensAndStoresSession() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("minh")
                .email("minh@gmail.com")
                .passwordHash("password-hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        LoginRequest request = LoginRequest.builder()
                .email("minh@gmail.com")
                .password("12345678")
                .build();
        when(userRepository.findByEmail("minh@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("12345678", "password-hash")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh-token");
        when(passwordEncoder.encode("refresh-token")).thenReturn("refresh-hash");
        when(httpServletRequest.getHeader(anyString())).thenAnswer(invocation -> {
            String header = invocation.getArgument(0);
            return "User-Agent".equals(header) ? "JUnit" : null;
        });
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        TokenResponse response = authService.login(request, httpServletRequest);

        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository).save(sessionCaptor.capture());
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getExpiresIn()).isEqualTo(900);
        assertThat(sessionCaptor.getValue().getRefreshTokenHash()).isEqualTo("refresh-hash");
        assertThat(user.getLastLoginAt()).isNotNull();
    }
}
