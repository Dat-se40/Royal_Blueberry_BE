package com.example.Royal_Blueberry.security;

import com.example.Royal_Blueberry.entity.User;
import com.example.Royal_Blueberry.util.Role;
import com.example.Royal_Blueberry.util.TokenType;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "accessKey", encodedKey("access-secret-key-access-secret-key-1234"));
        ReflectionTestUtils.setField(provider, "refreshKey", encodedKey("refresh-secret-key-refresh-secret-1234"));
        ReflectionTestUtils.setField(provider, "accessTokenExpiration", 30);
        ReflectionTestUtils.setField(provider, "refreshTokenExpiration", 60);

        User user = User.builder()
                .id("user-1")
                .email("user@example.com")
                .role(Role.USER)
                .build();
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void accessTokenCanBeGeneratedValidatedAndParsed() {
        String token = provider.generateAccessToken(userDetails);

        assertNotNull(token);
        assertTrue(provider.validateToken(token, TokenType.ACCESS));
        assertEquals("user-1", provider.getUserIdFromToken(token, TokenType.ACCESS));
        assertFalse(provider.isRefreshToken(token));
        assertEquals(30L, provider.getAccessTokenExpiration());
    }

    @Test
    void refreshTokenCanBeGeneratedAndRecognized() {
        String token = provider.generateRefreshToken(userDetails);

        assertTrue(provider.validateToken(token, TokenType.REFRESH));
        assertTrue(provider.isRefreshToken(token));
        assertEquals("user-1", provider.getUserIdFromToken(token, TokenType.REFRESH));
    }

    @Test
    void validateTokenWithWrongKeyTypeThrowsSignatureException() {
        String accessToken = provider.generateAccessToken(userDetails);

        assertThrows(SignatureException.class,
                () -> provider.validateToken(accessToken, TokenType.REFRESH));
    }

    @Test
    void oauthStateTokenValidationRequiresMatchingProvider() {
        String stateToken = provider.generateOAuthStateToken("google");

        assertTrue(provider.validateOAuthStateToken(stateToken, "google"));
        assertFalse(provider.validateOAuthStateToken(stateToken, "github"));
    }

    @Test
    void invalidTokensReturnFalse() {
        assertFalse(provider.validateToken("not-a-jwt", TokenType.ACCESS));
        assertFalse(provider.validateOAuthStateToken("not-a-jwt", "google"));
    }

    private String encodedKey(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
