package com.auction.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiResponse Tests")
class ApiResponseTest {

    @Nested
    @DisplayName("success() factory methods")
    class SuccessFactoryTests {

        @Test
        @DisplayName("success(data) should set success=true and data")
        void shouldCreateSuccessWithData() {
            ApiResponse<String> response = ApiResponse.success("hello");

            assertTrue(response.isSuccess());
            assertEquals("hello", response.getData());
            assertNull(response.getMessage());
            assertNull(response.getErrors());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("success(message, data) should set message and data")
        void shouldCreateSuccessWithMessageAndData() {
            ApiResponse<Integer> response = ApiResponse.success("Count found", 42);

            assertTrue(response.isSuccess());
            assertEquals("Count found", response.getMessage());
            assertEquals(42, response.getData());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("success(null) should have null data")
        void shouldAllowNullData() {
            ApiResponse<Void> response = ApiResponse.success("Done", null);
            assertTrue(response.isSuccess());
            assertEquals("Done", response.getMessage());
            assertNull(response.getData());
        }
    }

    @Nested
    @DisplayName("error() factory methods")
    class ErrorFactoryTests {

        @Test
        @DisplayName("error(message) should set success=false")
        void shouldCreateError() {
            ApiResponse<Void> response = ApiResponse.error("Something went wrong");

            assertFalse(response.isSuccess());
            assertEquals("Something went wrong", response.getMessage());
            assertNull(response.getData());
            assertNull(response.getErrors());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("error(message, errors) should include validation errors map")
        void shouldCreateErrorWithValidationErrors() {
            Map<String, String> errors = Map.of("title", "required", "price", "must be > 0");
            ApiResponse<Void> response = ApiResponse.error("Validation failed", errors);

            assertFalse(response.isSuccess());
            assertEquals("Validation failed", response.getMessage());
            assertNotNull(response.getErrors());
            assertEquals(2, response.getErrors().size());
            assertEquals("required", response.getErrors().get("title"));
            assertEquals("must be > 0", response.getErrors().get("price"));
        }
    }

    @Nested
    @DisplayName("Builder / POJO tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build with all fields")
        void shouldBuildWithAllFields() {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("msg")
                    .data("data")
                    .errors(Map.of("k", "v"))
                    .build();

            assertTrue(response.isSuccess());
            assertEquals("msg", response.getMessage());
            assertEquals("data", response.getData());
            assertEquals("v", response.getErrors().get("k"));
        }

        @Test
        @DisplayName("No-arg constructor should work")
        void noArgShouldWork() {
            ApiResponse<Void> response = new ApiResponse<>();
            assertFalse(response.isSuccess()); // default boolean is false
            assertNull(response.getMessage());
        }

        @Test
        @DisplayName("Setter/getter round-trip")
        void setterGetterTrip() {
            ApiResponse<Integer> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setMessage("ok");
            response.setData(99);

            assertTrue(response.isSuccess());
            assertEquals("ok", response.getMessage());
            assertEquals(99, response.getData());
        }
    }
}
