package com.auction.service;

import com.auction.dto.*;
import com.auction.entity.User;
import com.auction.exception.BadRequestException;
import com.auction.exception.DuplicateResourceException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.repository.UserRepository;
import com.auction.security.CustomUserDetailsService;
import com.auction.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .fullName("Test User")
                .phone("+91 9876543210")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");
        registerRequest.setPhone("+91 1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterSuccessfully() {
            // Arrange
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded_123");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(2L);
                return u;
            });

            Authentication mockAuth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(tokenProvider.generateAccessToken(mockAuth)).thenReturn("access_token_abc");
            when(tokenProvider.generateRefreshToken(mockAuth)).thenReturn("refresh_token_xyz");
            when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertNotNull(response);
            assertEquals("access_token_abc", response.getAccessToken());
            assertEquals("refresh_token_xyz", response.getRefreshToken());
            assertEquals("Bearer", response.getTokenType());
            assertEquals(86400000L, response.getExpiresIn());
            assertNotNull(response.getUser());
            assertEquals("newuser", response.getUser().getUsername());

            verify(userRepository).existsByUsername("newuser");
            verify(userRepository).existsByEmail("new@example.com");
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException for existing username")
        void shouldThrowForDuplicateUsername() {
            when(userRepository.existsByUsername("newuser")).thenReturn(true);

            DuplicateResourceException ex = assertThrows(
                    DuplicateResourceException.class,
                    () -> authService.register(registerRequest));

            assertTrue(ex.getMessage().contains("newuser"));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException for existing email")
        void shouldThrowForDuplicateEmail() {
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

            DuplicateResourceException ex = assertThrows(
                    DuplicateResourceException.class,
                    () -> authService.register(registerRequest));

            assertTrue(ex.getMessage().contains("new@example.com"));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() {
            Authentication mockAuth = mock(Authentication.class);
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            when(mockAuth.getPrincipal()).thenReturn(principal);
            when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
            when(tokenProvider.generateAccessToken(mockAuth)).thenReturn("access_token");
            when(tokenProvider.generateRefreshToken(mockAuth)).thenReturn("refresh_token");
            when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

            AuthResponse response = authService.login(loginRequest);

            assertNotNull(response);
            assertEquals("access_token", response.getAccessToken());
            assertEquals("refresh_token", response.getRefreshToken());
            assertEquals("Bearer", response.getTokenType());
            assertEquals("testuser", response.getUser().getUsername());
            assertEquals("test@example.com", response.getUser().getEmail());
        }
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return current authenticated user")
        void shouldReturnCurrentUser() {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);

            Authentication mockAuth = mock(Authentication.class);
            when(mockAuth.getPrincipal()).thenReturn(principal);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(mockAuth);
            SecurityContextHolder.setContext(securityContext);

            UserResponse response = authService.getCurrentUser();

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("testuser", response.getUsername());
            assertEquals("test@example.com", response.getEmail());
            assertEquals("Test User", response.getFullName());

            // Cleanup
            SecurityContextHolder.clearContext();
        }
    }

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile fields")
        void shouldUpdateProfile() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFullName("Updated Name");
            request.setPhone("+91 1111111111");

            UserResponse response = authService.updateProfile(1L, request);

            assertNotNull(response);
            assertEquals("Updated Name", testUser.getFullName());
            assertEquals("+91 1111111111", testUser.getPhone());
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw when updating email to already-used email")
        void shouldThrowForDuplicateEmailOnUpdate() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setEmail("taken@example.com");

            assertThrows(DuplicateResourceException.class,
                    () -> authService.updateProfile(1L, request));
        }

        @Test
        @DisplayName("Should allow keeping the same email")
        void shouldAllowSameEmail() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setEmail("test@example.com"); // same as current

            UserResponse response = authService.updateProfile(1L, request);
            assertNotNull(response);
            // Should NOT check existsByEmail since email didn't change
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent user")
        void shouldThrowForNonExistentUser() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFullName("Nobody");

            assertThrows(ResourceNotFoundException.class,
                    () -> authService.updateProfile(999L, request));
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldpass", "encoded_password")).thenReturn(true);
            when(passwordEncoder.encode("newpass")).thenReturn("new_encoded");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            assertDoesNotThrow(() -> authService.changePassword(1L, "oldpass", "newpass"));

            verify(passwordEncoder).encode("newpass");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw BadRequestException for incorrect current password")
        void shouldThrowForIncorrectPassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongpass", "encoded_password")).thenReturn(false);

            BadRequestException ex = assertThrows(
                    BadRequestException.class,
                    () -> authService.changePassword(1L, "wrongpass", "newpass"));

            assertTrue(ex.getMessage().contains("incorrect"));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent user")
        void shouldThrowForNonExistentUser() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> authService.changePassword(999L, "old", "new"));
        }
    }
}
