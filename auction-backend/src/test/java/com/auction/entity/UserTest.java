package com.auction.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Nested
    @DisplayName("Builder & Defaults")
    class BuilderTests {

        @Test
        @DisplayName("Should build user with all fields")
        void shouldBuildUserWithAllFields() {
            User user = User.builder()
                    .id(1L)
                    .username("testuser")
                    .email("test@example.com")
                    .password("encoded_password")
                    .fullName("Test User")
                    .phone("+91 9876543210")
                    .avatarUrl("https://example.com/avatar.jpg")
                    .role(User.Role.USER)
                    .enabled(true)
                    .emailVerified(false)
                    .build();

            assertEquals(1L, user.getId());
            assertEquals("testuser", user.getUsername());
            assertEquals("test@example.com", user.getEmail());
            assertEquals("encoded_password", user.getPassword());
            assertEquals("Test User", user.getFullName());
            assertEquals("+91 9876543210", user.getPhone());
            assertEquals("https://example.com/avatar.jpg", user.getAvatarUrl());
            assertEquals(User.Role.USER, user.getRole());
            assertTrue(user.getEnabled());
            assertFalse(user.getEmailVerified());
        }

        @Test
        @DisplayName("Should set correct default values via builder")
        void shouldSetDefaultValues() {
            User user = User.builder()
                    .username("defaultuser")
                    .email("default@example.com")
                    .password("password")
                    .build();

            assertTrue(user.getEnabled(), "Enabled should default to true");
            assertFalse(user.getEmailVerified(), "Email verified should default to false");
            assertEquals(User.Role.USER, user.getRole(), "Role should default to USER");
            assertNotNull(user.getAuctionItems(), "Auction items set should be initialized");
            assertNotNull(user.getBids(), "Bids set should be initialized");
            assertTrue(user.getAuctionItems().isEmpty());
            assertTrue(user.getBids().isEmpty());
        }

        @Test
        @DisplayName("Should create user with no-arg constructor")
        void shouldCreateWithNoArgConstructor() {
            User user = new User();
            assertNull(user.getId());
            assertNull(user.getUsername());
            assertNull(user.getEmail());
        }
    }

    @Nested
    @DisplayName("Getters & Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set all fields")
        void shouldGetAndSetAllFields() {
            User user = new User();

            user.setId(42L);
            user.setUsername("updated_user");
            user.setEmail("updated@example.com");
            user.setPassword("new_password");
            user.setFullName("Updated Name");
            user.setPhone("+91 1234567890");
            user.setAvatarUrl("https://new-avatar.jpg");
            user.setEnabled(false);
            user.setEmailVerified(true);
            user.setRole(User.Role.ADMIN);

            assertEquals(42L, user.getId());
            assertEquals("updated_user", user.getUsername());
            assertEquals("updated@example.com", user.getEmail());
            assertEquals("new_password", user.getPassword());
            assertEquals("Updated Name", user.getFullName());
            assertEquals("+91 1234567890", user.getPhone());
            assertEquals("https://new-avatar.jpg", user.getAvatarUrl());
            assertFalse(user.getEnabled());
            assertTrue(user.getEmailVerified());
            assertEquals(User.Role.ADMIN, user.getRole());
        }
    }

    @Nested
    @DisplayName("Role Enum")
    class RoleEnumTests {

        @Test
        @DisplayName("Should have USER and ADMIN roles")
        void shouldHaveTwoRoles() {
            User.Role[] roles = User.Role.values();
            assertEquals(2, roles.length);
            assertEquals(User.Role.USER, User.Role.valueOf("USER"));
            assertEquals(User.Role.ADMIN, User.Role.valueOf("ADMIN"));
        }
    }
}
