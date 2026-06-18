package com.apibe.API_BE.module.auth.security;

import com.apibe.API_BE.global.enums.UserRole;
import com.apibe.API_BE.global.enums.UserStatus;
import com.apibe.API_BE.global.security.JwtTokenProvider;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend.oauth-success-url:http://localhost:5173/oauth-success}")
    private String oauthSuccessUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google account email is required");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(generateUsername(email))
                        .email(email)
                        .name(resolveName(oauth2User, email))
                        .avatarUrl(oauth2User.getAttribute("picture"))
                        .role(UserRole.USER)
                        .status(UserStatus.ACTIVE)
                        .build()));

        String redirectUrl = UriComponentsBuilder.fromUriString(oauthSuccessUrl)
                .queryParam("token", jwtTokenProvider.generateAccessToken(user))
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    private String resolveName(OAuth2User oauth2User, String email) {
        String name = oauth2User.getAttribute("name");
        return name == null || name.isBlank() ? email.substring(0, email.indexOf("@")) : name;
    }

    private String generateUsername(String email) {
        return email.substring(0, email.indexOf("@")) + "_" + Math.abs(email.hashCode());
    }
}
