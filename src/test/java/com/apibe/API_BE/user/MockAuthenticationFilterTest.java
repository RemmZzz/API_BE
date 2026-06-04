package com.apibe.API_BE.user;

import com.apibe.API_BE.global.enums.UserRole;
import com.apibe.API_BE.global.security.MockAuthenticationFilter;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockAuthenticationFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private MockAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new MockAuthenticationFilter(userRepository, jdbcTemplate);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testFilterWithValidUuidToken() throws Exception {
        UUID expectedUuid = UUID.randomUUID();
        lenient().when(request.getServletPath()).thenReturn("/api/projects");
        lenient().when(request.getHeader("X-User-Id")).thenReturn(null);
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer " + expectedUuid);

        User mockUser = new User();
        mockUser.setId(expectedUuid);
        mockUser.setUsername("mockuser_" + expectedUuid.toString().substring(0, 8));
        mockUser.setRole(UserRole.USER);

        lenient().when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterWithMockTokenString() throws Exception {
        String mockToken = "mock-token-admin-1-1717523948000";
        UUID expectedUuid = UUID.nameUUIDFromBytes("admin-1".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        lenient().when(request.getServletPath()).thenReturn("/api/projects");
        lenient().when(request.getHeader("X-User-Id")).thenReturn(null);
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer " + mockToken);

        User mockUser = new User();
        mockUser.setId(expectedUuid);
        mockUser.setUsername("mock_admin-1");
        mockUser.setRole(UserRole.ADMIN);

        lenient().when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("mock_admin-1", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }
}
