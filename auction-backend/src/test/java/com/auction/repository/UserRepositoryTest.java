package com.auction.repository;

import com.auction.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("encoded_password_123")
                .fullName("John Doe")
                .phone("+91 9876543210")
                .role(User.Role.USER)
                .enabled(true)
                .emailVerified(false)
                .build();
        entityManager.persistAndFlush(testUser);
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should find user by existing username")
        void shouldFindByExistingUsername() {
            Optional<User> found = userRepository.findByUsername("johndoe");

            assertTrue(found.isPresent());
            assertEquals("johndoe", found.get().getUsername());
            assertEquals("john@example.com", found.get().getEmail());
            assertEquals("John Doe", found.get().getFullName());
        }

        @Test
        @DisplayName("Should return empty for non-existent username")
        void shouldReturnEmptyForNonExistentUsername() {
            Optional<User> found = userRepository.findByUsername("nonexistent");
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user by existing email")
        void shouldFindByExistingEmail() {
            Optional<User> found = userRepository.findByEmail("john@example.com");

            assertTrue(found.isPresent());
            assertEquals("john@example.com", found.get().getEmail());
        }

        @Test
        @DisplayName("Should return empty for non-existent email")
        void shouldReturnEmptyForNonExistentEmail() {
            Optional<User> found = userRepository.findByEmail("nobody@example.com");
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByUsernameOrEmail")
    class FindByUsernameOrEmailTests {

        @Test
        @DisplayName("Should find user by username (first param)")
        void shouldFindByUsername() {
            Optional<User> found = userRepository.findByUsernameOrEmail("johndoe", "johndoe");
            assertTrue(found.isPresent());
            assertEquals("johndoe", found.get().getUsername());
        }

        @Test
        @DisplayName("Should find user by email (second param)")
        void shouldFindByEmail() {
            Optional<User> found = userRepository.findByUsernameOrEmail("john@example.com", "john@example.com");
            assertTrue(found.isPresent());
            assertEquals("john@example.com", found.get().getEmail());
        }

        @Test
        @DisplayName("Should return empty when neither matches")
        void shouldReturnEmptyWhenNeitherMatches() {
            Optional<User> found = userRepository.findByUsernameOrEmail("wrong", "wrong@wrong.com");
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("existsByUsername / existsByEmail")
    class ExistenceCheckTests {

        @Test
        @DisplayName("existsByUsername should return true for existing username")
        void existsByUsernameShouldReturnTrue() {
            assertTrue(userRepository.existsByUsername("johndoe"));
        }

        @Test
        @DisplayName("existsByUsername should return false for non-existent username")
        void existsByUsernameShouldReturnFalse() {
            assertFalse(userRepository.existsByUsername("nobody"));
        }

        @Test
        @DisplayName("existsByEmail should return true for existing email")
        void existsByEmailShouldReturnTrue() {
            assertTrue(userRepository.existsByEmail("john@example.com"));
        }

        @Test
        @DisplayName("existsByEmail should return false for non-existent email")
        void existsByEmailShouldReturnFalse() {
            assertFalse(userRepository.existsByEmail("nobody@example.com"));
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("Should save and retrieve user by id")
        void shouldSaveAndFind() {
            User newUser = User.builder()
                    .username("janedoe")
                    .email("jane@example.com")
                    .password("password")
                    .fullName("Jane Doe")
                    .build();

            User saved = userRepository.save(newUser);
            assertNotNull(saved.getId());

            Optional<User> found = userRepository.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals("janedoe", found.get().getUsername());
        }

        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() {
            Long id = testUser.getId();
            assertTrue(userRepository.findById(id).isPresent());

            userRepository.deleteById(id);
            entityManager.flush();

            assertFalse(userRepository.findById(id).isPresent());
        }

        @Test
        @DisplayName("Should count users correctly")
        void shouldCountUsers() {
            assertEquals(1, userRepository.count());

            User another = User.builder()
                    .username("second")
                    .email("second@example.com")
                    .password("pass")
                    .build();
            userRepository.save(another);

            assertEquals(2, userRepository.count());
        }
    }
}
