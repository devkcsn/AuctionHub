package com.auction.security;

import com.auction.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "myTestSecretKeyForJWTTesting12345");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 86400000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpirationMs", 604800000L);
    }

    private Authentication createAuthentication(User user) {
        CustomUserDetailsService.CustomUserPrincipal principal =
                new CustomUserDetailsService.CustomUserPrincipal(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .fullName("Test User")
                .role(User.Role.USER)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("generateAccessToken()")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("Should generate a non-null JWT access token")
        void shouldGenerateAccessToken() {
            Authentication auth = createAuthentication(createTestUser());
            String token = tokenProvider.generateAccessToken(auth);
            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void shouldGenerateDifferentTokens() {
            User user1 = createTestUser();
            User user2 = User.builder()
                    .id(2L).username("other").email("o@e.com").password("p")
                    .role(User.Role.USER).enabled(true).build();

            String token1 = tokenProvider.generateAccessToken(createAuthentication(user1));
            String token2 = tokenProvider.generateAccessToken(createAuthentication(user2));

            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("generateRefreshToken()")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should generate a non-null JWT refresh token")
        void shouldGenerateRefreshToken() {
            Authentication auth = createAuthentication(createTestUser());
            String token = tokenProvider.generateRefreshToken(auth);
            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @Test
        @DisplayName("Should differ from access token")
        void shouldDifferFromAccessToken() {
            Authentication auth = createAuthentication(createTestUser());
            String access = tokenProvider.generateAccessToken(auth);
            String refresh = tokenProvider.generateRefreshToken(auth);
            assertNotEquals(access, refresh);
        }
    }

    @Nested
    @DisplayName("getUserIdFromToken()")
    class GetUserIdTests {

        @Test
        @DisplayName("Should extract user ID from valid token")
        void shouldExtractUserId() {
            Authentication auth = createAuthentication(createTestUser());
            String token = tokenProvider.generateAccessToken(auth);
            Long userId = tokenProvider.getUserIdFromToken(token);
            assertEquals(1L, userId);
        }
    }

    @Nested
    @DisplayName("validateToken()")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return true for a valid token")
        void shouldValidateValidToken() {
            Authentication auth = createAuthentication(createTestUser());
            String token = tokenProvider.generateAccessToken(auth);
            assertTrue(tokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("Should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", -1000L);
            Authentication auth = createAuthentication(createTestUser());
            String token = tokenProvider.generateAccessToken(auth);
            assertFalse(tokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("Should return false for malformed token")
        void shouldReturnFalseForMalformedToken() {
            assertFalse(tokenProvider.validateToken("not.a.valid.jwt.token"));
        }

        @Test
        @DisplayName("Should return false for null token")
        void shouldReturnFalseForNullToken() {
            assertFalse(tokenProvider.validateToken(null));
        }

        @Test
        @DisplayName("Should return false for empty token")
        void shouldReturnFalseForEmptyToken() {
            assertFalse(tokenProvider.validateToken(""));
        }

        @Test
        @DisplayName("Should return false for token signed with different key")
        void shouldReturnFalseForWrongSignature() {
            Authentication auth = createAuthentication(createTestUser());
            String token = tokenProvider.generateAccessToken(auth);

            // Create a new provider with a different secret
            JwtTokenProvider otherProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(otherProvider, "jwtSecret", "aDifferentSecretKeyForTesting12345");
            ReflectionTestUtils.setField(otherProvider, "jwtExpirationMs", 86400000L);
            ReflectionTestUtils.setField(otherProvider, "refreshExpirationMs", 604800000L);

            assertFalse(otherProvider.validateToken(token));
        }
    }

    @Nested
    @DisplayName("getExpirationMs()")
    class GetExpirationMsTests {

        @Test
        @DisplayName("Should return configured expiration")
        void shouldReturnConfiguredExpiration() {
            assertEquals(86400000L, tokenProvider.getExpirationMs());
        }
    }
}
