package com.auction.security;

import com.auction.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenProvider tokenProvider;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should set authentication for valid token")
    void shouldSetAuthForValidToken() throws ServletException, IOException {
        User user = User.builder()
                .id(1L).username("testuser").email("t@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();

        CustomUserDetailsService.CustomUserPrincipal principal =
                new CustomUserDetailsService.CustomUserPrincipal(user);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token_here");
        when(tokenProvider.validateToken("valid_token_here")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("valid_token_here")).thenReturn(1L);
        when(userDetailsService.loadUserById(1L)).thenReturn(principal);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication when no Authorization header")
    void shouldNotSetAuthWhenNoHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication for invalid token")
    void shouldNotSetAuthForInvalidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(tokenProvider.validateToken("invalid_token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication for non-Bearer token")
    void shouldNotSetAuthForNonBearerToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should continue filter chain even on exception")
    void shouldContinueOnException() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(tokenProvider.validateToken("valid_token")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("valid_token")).thenThrow(new RuntimeException("DB down"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should extract token from Bearer header correctly")
    void shouldExtractTokenFromBearerHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer abc.def.ghi");
        when(tokenProvider.validateToken("abc.def.ghi")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(tokenProvider).validateToken("abc.def.ghi");
        verify(filterChain).doFilter(request, response);
    }
}
