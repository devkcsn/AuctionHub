package com.auction.dto;

import com.auction.entity.AuctionItem;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Validation Tests")
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("RegisterRequest validation")
    class RegisterRequestTests {

        @Test
        @DisplayName("Valid request should have no violations")
        void validRequest() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testuser");
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFullName("Test User");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Blank username should have violation")
        void blankUsername() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("");
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFullName("Test");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        }

        @Test
        @DisplayName("Short username should have violation")
        void shortUsername() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("ab");
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFullName("Test");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Invalid email should have violation")
        void invalidEmail() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testuser");
            req.setEmail("not-an-email");
            req.setPassword("password123");
            req.setFullName("Test");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        }

        @Test
        @DisplayName("Null password should have violation")
        void nullPassword() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testuser");
            req.setEmail("test@example.com");
            req.setPassword(null);
            req.setFullName("Test");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Short password should have violation")
        void shortPassword() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testuser");
            req.setEmail("test@example.com");
            req.setPassword("abc");
            req.setFullName("Test");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Blank fullName should have violation")
        void blankFullName() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testuser");
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFullName("");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Phone is optional")
        void phoneIsOptional() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testuser");
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFullName("Test User");
            req.setPhone(null);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("LoginRequest validation")
    class LoginRequestTests {

        @Test
        @DisplayName("Valid login should have no violations")
        void validLogin() {
            LoginRequest req = new LoginRequest();
            req.setUsernameOrEmail("testuser");
            req.setPassword("password123");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Blank usernameOrEmail should have violation")
        void blankUsername() {
            LoginRequest req = new LoginRequest();
            req.setUsernameOrEmail("");
            req.setPassword("password123");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Blank password should have violation")
        void blankPassword() {
            LoginRequest req = new LoginRequest();
            req.setUsernameOrEmail("test");
            req.setPassword("");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("ChangePasswordRequest validation")
    class ChangePasswordRequestTests {

        @Test
        @DisplayName("Valid request should have no violations")
        void validRequest() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("oldpass");
            req.setNewPassword("newpass123");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(req);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Blank current password should have violation")
        void blankCurrentPassword() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("");
            req.setNewPassword("newpass123");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Short new password should have violation")
        void shortNewPassword() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("oldpass");
            req.setNewPassword("ab");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("BidRequest validation")
    class BidRequestTests {

        @Test
        @DisplayName("Valid request should have no violations")
        void validRequest() {
            BidRequest req = new BidRequest();
            req.setAuctionItemId(10L);
            req.setAmount(new BigDecimal("100.00"));

            Set<ConstraintViolation<BidRequest>> violations = validator.validate(req);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Null auctionItemId should have violation")
        void nullAuctionItemId() {
            BidRequest req = new BidRequest();
            req.setAmount(new BigDecimal("100.00"));

            Set<ConstraintViolation<BidRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Null amount should have violation")
        void nullAmount() {
            BidRequest req = new BidRequest();
            req.setAuctionItemId(10L);

            Set<ConstraintViolation<BidRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Zero amount should have violation")
        void zeroAmount() {
            BidRequest req = new BidRequest();
            req.setAuctionItemId(10L);
            req.setAmount(BigDecimal.ZERO);

            Set<ConstraintViolation<BidRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("AuctionItemRequest validation")
    class AuctionItemRequestTests {

        private AuctionItemRequest validRequest() {
            AuctionItemRequest req = new AuctionItemRequest();
            req.setTitle("Test Auction");
            req.setDescription("A test item description");
            req.setStartingPrice(new BigDecimal("100.00"));
            req.setCategory(AuctionItem.Category.ELECTRONICS);
            req.setStartTime(LocalDateTime.now().plusHours(1));
            req.setEndTime(LocalDateTime.now().plusDays(3));
            return req;
        }

        @Test
        @DisplayName("Valid request should have no violations")
        void valid() {
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(validRequest());
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Blank title should have violation")
        void blankTitle() {
            AuctionItemRequest req = validRequest();
            req.setTitle("");
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Null starting price should have violation")
        void nullStartingPrice() {
            AuctionItemRequest req = validRequest();
            req.setStartingPrice(null);
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Zero starting price should have violation")
        void zeroStartingPrice() {
            AuctionItemRequest req = validRequest();
            req.setStartingPrice(BigDecimal.ZERO);
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Null category should have violation")
        void nullCategory() {
            AuctionItemRequest req = validRequest();
            req.setCategory(null);
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Null start time should have violation")
        void nullStartTime() {
            AuctionItemRequest req = validRequest();
            req.setStartTime(null);
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Null end time should have violation")
        void nullEndTime() {
            AuctionItemRequest req = validRequest();
            req.setEndTime(null);
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Title over 200 chars should have violation")
        void longTitle() {
            AuctionItemRequest req = validRequest();
            req.setTitle("A".repeat(201));
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(req);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Reserve price and min bid increment are optional")
        void optionalFields() {
            AuctionItemRequest req = validRequest();
            req.setReservePrice(null);
            req.setMinBidIncrement(null);
            Set<ConstraintViolation<AuctionItemRequest>> violations = validator.validate(req);
            assertTrue(violations.isEmpty());
        }
    }
}
