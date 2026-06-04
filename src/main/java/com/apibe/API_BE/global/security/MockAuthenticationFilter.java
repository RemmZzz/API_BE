package com.apibe.API_BE.global.security;

import com.apibe.API_BE.global.enums.UserRole;
import com.apibe.API_BE.global.enums.UserStatus;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Profile("!prod")
public class MockAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public MockAuthenticationFilter(UserRepository userRepository,
                                    JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        UUID userId = null;
        String identifier = null;

        // 1. Try to extract from X-User-Id
        String xUserId = request.getHeader("X-User-Id");
        if (xUserId != null && !xUserId.isBlank()) {
            try {
                userId = UUID.fromString(xUserId.trim());
            } catch (IllegalArgumentException ignored) {
                identifier = xUserId.trim();
            }
        }

        // 2. Try to extract from Authorization
        if (userId == null && identifier == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                String token = authHeader.trim();
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7).trim();
                }
                try {
                    userId = UUID.fromString(token);
                } catch (IllegalArgumentException ignored) {
                    if (token.startsWith("mock-token-")) {
                        int lastHyphen = token.lastIndexOf('-');
                        if (lastHyphen > 11) {
                            identifier = token.substring(11, lastHyphen);
                        } else {
                            identifier = token.substring(11);
                        }
                    } else {
                        identifier = token;
                    }
                }
            }
        }

        // 3. Fallback or map identifier to UUID
        if (userId == null) {
            if (identifier != null && !identifier.isBlank()) {
                userId = UUID.nameUUIDFromBytes(identifier.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } else {
                userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
                identifier = "default-mock";
            }
        } else if (identifier == null) {
            identifier = userId.toString();
        }

        // 4. Find or create user
        final UUID finalUserId = userId;
        final String finalIdentifier = identifier;
        ensureMockUserExists(finalUserId, finalIdentifier);
        User user = userRepository.findByUsername(mockUsername(finalUserId, finalIdentifier))
                .orElseThrow(() -> new ServletException("Cannot initialize mock user " + finalUserId));

        // 5. Put in SecurityContext
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void ensureMockUserExists(UUID userId, String identifier) {
        String userIdText = userId.toString();
        String suffix = mockUserSuffix(userId);

        UserRole role = UserRole.USER;
        if (identifier != null && identifier.toLowerCase().contains("admin")) {
            role = UserRole.ADMIN;
        }

        jdbcTemplate.update("""
                INSERT IGNORE INTO users
                    (id, username, email, name, password_hash, role, status, created_at, updated_at)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, NOW(6), NOW(6))
                """,
                userIdText,
                mockUsername(userId, identifier),
                "mock_" + suffix + "@example.com",
                "Mock User " + (identifier != null ? identifier : ""),
                "$2a$10$U83qP7x/B1yHSwQJtOqCGOt93X4kHk1h8V38n8W5XJ8P7c3U1E1yq",
                role.name(),
                UserStatus.ACTIVE.name());

        jdbcTemplate.update("""
                INSERT IGNORE INTO user_settings
                    (id, user_id, language, notification_settings, privacy_settings, created_at, updated_at)
                VALUES
                    (?, ?, ?, ?, ?, NOW(6), NOW(6))
                """,
                UUID.randomUUID().toString(),
                userIdText,
                "vi",
                "{}",
                "{}");
    }

    private String mockUsername(UUID userId, String identifier) {
        if (identifier != null && !identifier.equals(userId.toString()) && !identifier.equals("default-mock")) {
            return "mock_" + identifier;
        }
        return "mockuser_" + mockUserSuffix(userId);
    }

    private String mockUserSuffix(UUID userId) {
        return userId.toString().substring(0, 8);
    }
}
