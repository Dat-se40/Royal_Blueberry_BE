package com.example.Royal_Blueberry.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final ServletWebRequest request =
            new ServletWebRequest(new MockHttpServletRequest("GET", "/api/test"));

    @Test
    void handleAuthExceptionUsesProvidedStatusAndMessage() {
        var response = handler.handleAuthException(
                new AuthException("Conflict", HttpStatus.CONFLICT),
                request
        );

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflict", response.getBody().get("message"));
    }

    @Test
    void handleBadCredentialsReturnsUnauthorizedMessage() {
        var response = handler.handleBadCredentials(new BadCredentialsException("bad"), request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid email or password", response.getBody().get("message"));
    }

    @Test
    void handleAccessDeniedReturnsForbiddenMessage() {
        var response = handler.handleAccessDenied(
                new AccessDeniedException("forbidden"),
                request
        );

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Access denied. Insufficient permissions.", response.getBody().get("message"));
    }

    @Test
    void handleRuntimeExceptionReturnsInternalServerError() {
        var response = handler.handleRuntimeException(new RuntimeException("boom"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("boom", response.getBody().get("message"));
    }

    @Test
    void handleGenericExceptionReturnsFallbackMessage() {
        var response = handler.handleGenericException(new Exception("boom"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }
}
