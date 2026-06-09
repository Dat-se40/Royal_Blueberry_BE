package com.example.Royal_Blueberry.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void shouldNotFilterSwaggerAndStaticAssets() {
        MockHttpServletRequest swaggerRequest = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/api/packages");

        assertTrue(filter.shouldNotFilter(swaggerRequest));
        assertFalse(filter.shouldNotFilter(apiRequest));
    }

    @Test
    void doFilterCopiesWrappedResponseBodyBackToClient() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/packages");
        request.addHeader("X-Forwarded-For", "1.1.1.1, 2.2.2.2");
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            httpResponse.setStatus(201);
            httpResponse.getWriter().write("created");
        };

        filter.doFilter(request, response, chain);

        assertEquals(201, response.getStatus());
        assertEquals("created", response.getContentAsString());
    }
}
