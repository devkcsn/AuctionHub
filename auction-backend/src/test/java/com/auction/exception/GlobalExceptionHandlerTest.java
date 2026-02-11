package com.auction.exception;

import com.auction.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("Should handle ResourceNotFoundException with 404")
    void shouldHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 1L);
        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should handle BadRequestException with 400")
    void shouldHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Invalid input");
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadRequestException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException with 409")
    void shouldHandleDuplicate() {
        DuplicateResourceException ex = new DuplicateResourceException("Email already exists");
        ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateResourceException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle UnauthorizedException with 403")
    void shouldHandleUnauthorized() {
        UnauthorizedException ex = new UnauthorizedException("Not your auction");
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorizedException(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle AuctionException with 400")
    void shouldHandleAuctionException() {
        AuctionException ex = new AuctionException("Auction not active");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuctionException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with field errors")
    void shouldHandleValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        FieldError fieldError = new FieldError("object", "title", "Title is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationErrors(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().getErrors());
        assertEquals("Title is required", response.getBody().getErrors().get("title"));
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException")
    void shouldHandleConstraintViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("createAuction.title");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ResponseEntity<ApiResponse<Void>> response = handler.handleConstraintViolation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must not be blank", response.getBody().getErrors().get("title"));
    }

    @Test
    @DisplayName("Should handle BadCredentialsException with 401")
    void shouldHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentials(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username/email or password", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle AuthenticationException with 401")
    void shouldHandleAuthException() {
        InternalAuthenticationServiceException ex =
                new InternalAuthenticationServiceException("Auth failed");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthenticationException(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle AccessDeniedException with 403")
    void shouldHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDeniedException(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("permission"));
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException with 409")
    void shouldHandleDataIntegrity() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint");
        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrityViolation(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException with 405")
    void shouldHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("DELETE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleMethodNotSupported(ex);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("DELETE"));
    }

    @Test
    @DisplayName("Should handle HttpMediaTypeNotSupportedException with 415")
    void shouldHandleMediaTypeNotSupported() {
        HttpMediaTypeNotSupportedException ex =
                new HttpMediaTypeNotSupportedException("text/plain is not supported");
        ResponseEntity<ApiResponse<Void>> response = handler.handleMediaTypeNotSupported(ex);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException with 400")
    void shouldHandleMessageNotReadable() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        ResponseEntity<ApiResponse<Void>> response = handler.handleMessageNotReadable(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Malformed"));
    }

    @Test
    @DisplayName("Should handle MissingServletRequestParameterException with 400")
    void shouldHandleMissingParam() throws Exception {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("keyword", "String");
        ResponseEntity<ApiResponse<Void>> response = handler.handleMissingParameter(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("keyword"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException with 400")
    void shouldHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getValue()).thenReturn("abc");
        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("id"));
    }

    @Test
    @DisplayName("Should handle MaxUploadSizeExceededException with 413")
    void shouldHandleMaxUploadSize() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024);
        ResponseEntity<ApiResponse<Void>> response = handler.handleMaxUploadSize(ex);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle NoHandlerFoundException with 404")
    void shouldHandleNoHandler() {
        NoHandlerFoundException ex =
                new NoHandlerFoundException("GET", "/api/unknown", null);
        ResponseEntity<ApiResponse<Void>> response = handler.handleNoHandlerFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("/api/unknown"));
    }

    @Test
    @DisplayName("Should handle generic Exception with 500")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAllUncaughtException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("unexpected"));
    }
}
