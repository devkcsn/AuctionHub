package com.auction.security;

import com.auction.entity.User;
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .password("encoded_password").fullName("Test User")
                .role(User.Role.USER).enabled(true).build();
    }

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should load user by username")
        void shouldLoadByUsername() {
            when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                    .thenReturn(Optional.of(testUser));

            UserDetails details = service.loadUserByUsername("testuser");

            assertNotNull(details);
            assertEquals("testuser", details.getUsername());
            assertEquals("encoded_password", details.getPassword());
            assertTrue(details.isEnabled());
            assertFalse(details.getAuthorities().isEmpty());
        }

        @Test
        @DisplayName("Should load user by email")
        void shouldLoadByEmail() {
            when(userRepository.findByUsernameOrEmail("test@example.com", "test@example.com"))
                    .thenReturn(Optional.of(testUser));

            UserDetails details = service.loadUserByUsername("test@example.com");
            assertNotNull(details);
            assertEquals("testuser", details.getUsername());
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException for unknown user")
        void shouldThrowForUnknown() {
            when(userRepository.findByUsernameOrEmail("unknown", "unknown"))
                    .thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> service.loadUserByUsername("unknown"));
        }
    }

    @Nested
    @DisplayName("loadUserById()")
    class LoadUserByIdTests {

        @Test
        @DisplayName("Should load user by id")
        void shouldLoadById() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserDetails details = service.loadUserById(1L);

            assertNotNull(details);
            assertEquals("testuser", details.getUsername());
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException for unknown id")
        void shouldThrowForUnknownId() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> service.loadUserById(999L));
        }
    }

    @Nested
    @DisplayName("CustomUserPrincipal")
    class CustomUserPrincipalTests {

        @Test
        @DisplayName("Should expose user id")
        void shouldExposeId() {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            assertEquals(1L, principal.getId());
        }

        @Test
        @DisplayName("Should expose email")
        void shouldExposeEmail() {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            assertEquals("test@example.com", principal.getEmail());
        }

        @Test
        @DisplayName("Should expose user entity")
        void shouldExposeUser() {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            assertEquals(testUser, principal.getUser());
        }

        @Test
        @DisplayName("Should return correct authorities for USER role")
        void shouldReturnUserAuthorities() {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            assertEquals(1, principal.getAuthorities().size());
            assertTrue(principal.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("Should return correct authorities for ADMIN role")
        void shouldReturnAdminAuthorities() {
            testUser.setRole(User.Role.ADMIN);
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            assertTrue(principal.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("Account should be non-expired, non-locked, credentials non-expired")
        void shouldBeValid() {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            assertTrue(principal.isAccountNonExpired());
            assertTrue(principal.isAccountNonLocked());
            assertTrue(principal.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("Should be enabled when user is enabled")
        void shouldBeEnabled() {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            assertTrue(principal.isEnabled());
        }

        @Test
        @DisplayName("Should be disabled when user is disabled")
        void shouldBeDisabled() {
            testUser.setEnabled(false);
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(testUser);
            assertFalse(principal.isEnabled());
        }
    }
}
