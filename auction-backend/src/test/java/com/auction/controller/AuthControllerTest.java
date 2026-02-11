package com.auction.controller;

import com.auction.dto.*;
import com.auction.entity.User;
import com.auction.security.CustomUserDetailsService;
import com.auction.security.JwtTokenProvider;
import com.auction.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;
    @MockitoBean private JwtTokenProvider tokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private UserResponse userResponse;
    private CustomUserDetailsService.CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .password("encoded").fullName("Test User").phone("+91 9876543210")
                .role(User.Role.USER).enabled(true).build();

        principal = new CustomUserDetailsService.CustomUserPrincipal(testUser);

        userResponse = UserResponse.builder()
                .id(1L).username("testuser").email("test@example.com")
                .fullName("Test User").phone("+91 9876543210")
                .role(User.Role.USER).createdAt(LocalDateTime.now()).build();

        authResponse = AuthResponse.builder()
                .accessToken("jwt_access_token")
                .refreshToken("jwt_refresh_token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userResponse)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");

        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @WithMockUser
        @DisplayName("Should register successfully and return 201")
        void shouldRegisterSuccessfully() throws Exception {
            when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Registration successful"))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt_access_token"))
                    .andExpect(jsonPath("$.data.user.username").value("testuser"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 for invalid request (blank username)")
        void shouldReturn400ForBlankUsername() throws Exception {
            registerRequest.setUsername("");

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmail() throws Exception {
            registerRequest.setEmail("not-an-email");

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 for short password")
        void shouldReturn400ForShortPassword() throws Exception {
            registerRequest.setPassword("ab");

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @WithMockUser
        @DisplayName("Should login successfully and return 200")
        void shouldLoginSuccessfully() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt_access_token"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 for blank username")
        void shouldReturn400ForBlankUsername() throws Exception {
            loginRequest.setUsernameOrEmail("");

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserEndpointTests {

        @Test
        @DisplayName("Should return current user when authenticated")
        void shouldReturnCurrentUser() throws Exception {
            when(authService.getCurrentUser()).thenReturn(userResponse);

            mockMvc.perform(get("/api/auth/me")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/profile")
    class UpdateProfileEndpointTests {

        @Test
        @DisplayName("Should update profile successfully")
        void shouldUpdateProfile() throws Exception {
            when(authService.updateProfile(eq(1L), any(UpdateProfileRequest.class)))
                    .thenReturn(userResponse);

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFullName("Updated Name");

            mockMvc.perform(put("/api/auth/profile")
                            .with(user(principal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Profile updated"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/change-password")
    class ChangePasswordEndpointTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePassword() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("oldpass123");
            request.setNewPassword("newpass123");

            mockMvc.perform(post("/api/auth/change-password")
                            .with(user(principal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Password changed successfully"));
        }

        @Test
        @DisplayName("Should return 400 for blank current password")
        void shouldReturn400ForBlankCurrentPassword() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("");
            request.setNewPassword("newpass123");

            mockMvc.perform(post("/api/auth/change-password")
                            .with(user(principal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
