package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.auth.AuthResponse;
import com.example.Royal_Blueberry.dto.auth.GoogleLoginRequest;
import com.example.Royal_Blueberry.dto.auth.GoogleLoginUrlResponse;
import com.example.Royal_Blueberry.dto.auth.LoginRequest;
import com.example.Royal_Blueberry.dto.auth.RefreshTokenRequest;
import com.example.Royal_Blueberry.dto.auth.RegisterRequest;
import com.example.Royal_Blueberry.entity.User;
import com.example.Royal_Blueberry.exception.AuthException;
import com.example.Royal_Blueberry.repository.UserRepository;
import com.example.Royal_Blueberry.security.CustomUserDetails;
import com.example.Royal_Blueberry.security.JwtTokenProvider;
import com.example.Royal_Blueberry.util.AuthProvider;
import com.example.Royal_Blueberry.util.Role;
import com.example.Royal_Blueberry.util.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
            authorizationCodeTokenResponseClient;

    @Mock
    private JwtDecoderFactory<ClientRegistration> oidcIdTokenDecoderFactory;

    @Mock
    private JwtDecoder jwtDecoder;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                jwtTokenProvider,
                authenticationManager,
                authorizationCodeTokenResponseClient,
                oidcIdTokenDecoderFactory,
                "google-client-id",
                "google-client-secret",
                "http://localhost/callback"
        );
    }

    @Test
    void loginNormalizesEmailAuthenticatesAndBuildsTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("  USER@example.com ");
        request.setPassword("secret");

        User user = user("u1", "user@example.com", AuthProvider.LOCAL);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(user));
        mockTokenGeneration(user);

        AuthResponse response = service.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("user@example.com", captor.getValue().getPrincipal());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("user@example.com", response.getUser().getEmail());
    }

    @Test
    void loginThrowsAuthExceptionForBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        AuthException exception = assertThrows(AuthException.class, () -> service.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void registerCreatesLocalUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("  USER@example.com ");
        request.setPassword("secret123");
        request.setDisplayName("User");

        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("u1");
            return user;
        });
        mockTokenGeneration(user("u1", "user@example.com", AuthProvider.LOCAL));

        AuthResponse response = service.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("user@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("User", savedUser.getDisplayName());
        assertEquals(Role.USER, savedUser.getRole());

        assertEquals("u1", response.getUser().getId());
        assertEquals("user@example.com", response.getUser().getEmail());
    }

    @Test
    void registerRejectsExistingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        AuthException exception = assertThrows(AuthException.class, () -> service.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void registerConvertsDuplicateKeyIntoConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("secret123");
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenThrow(new DuplicateKeyException("duplicate"));

        AuthException exception = assertThrows(AuthException.class, () -> service.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void getGoogleLoginUrlReturnsStateRedirectUriAndScope() {
        when(jwtTokenProvider.generateOAuthStateToken("google")).thenReturn("state-token");

        GoogleLoginUrlResponse response = service.getGoogleLoginUrl();

        assertEquals("state-token", response.getState());
        assertEquals("http://localhost/callback", response.getRedirectUri());
        assertEquals("openid email profile", response.getScope());
        assertTrue(response.getUrl().contains("google-client-id"));
        assertTrue(response.getUrl().contains(OAuth2ParameterNames.STATE + "=state-token"));
    }

    @Test
    void loginWithGoogleCreatesNewGoogleUser() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setCode("auth-code");
        request.setState("state-token");

        when(jwtTokenProvider.validateOAuthStateToken("state-token", "google")).thenReturn(true);
        when(authorizationCodeTokenResponseClient.getTokenResponse(any())).thenReturn(
                org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
                        .withToken("access")
                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                        .expiresIn(3600)
                        .additionalParameters(Map.of(OidcParameterNames.ID_TOKEN, "id-token"))
                        .build()
        );
        when(oidcIdTokenDecoderFactory.createDecoder(any(ClientRegistration.class))).thenReturn(jwtDecoder);
        when(jwtDecoder.decode("id-token")).thenReturn(googleJwt(
                "google-123",
                "USER@example.com",
                "Google User",
                "https://avatar",
                true
        ));
        when(userRepository.findByProviderAndGoogleId(AuthProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId("u1");
            }
            return user;
        });
        mockTokenGeneration(user("u1", "user@example.com", AuthProvider.GOOGLE));

        AuthResponse response = service.loginWithGoogle(request);

        assertEquals("u1", response.getUser().getId());
        assertEquals("user@example.com", response.getUser().getEmail());
        assertEquals("Google User", response.getUser().getDisplayName());
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void loginWithGoogleRejectsInvalidState() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setState("bad-state");
        when(jwtTokenProvider.validateOAuthStateToken("bad-state", "google")).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class,
                () -> service.loginWithGoogle(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid Google login state", exception.getMessage());
    }

    @Test
    void loginWithGoogleRejectsUnverifiedEmail() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setCode("auth-code");
        request.setState("state-token");
        when(jwtTokenProvider.validateOAuthStateToken("state-token", "google")).thenReturn(true);
        when(authorizationCodeTokenResponseClient.getTokenResponse(any())).thenReturn(
                org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
                        .withToken("access")
                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                        .expiresIn(3600)
                        .additionalParameters(Map.of(OidcParameterNames.ID_TOKEN, "id-token"))
                        .build()
        );
        when(oidcIdTokenDecoderFactory.createDecoder(any(ClientRegistration.class))).thenReturn(jwtDecoder);
        when(jwtDecoder.decode("id-token")).thenReturn(googleJwt(
                "google-123",
                "user@example.com",
                "Google User",
                "https://avatar",
                false
        ));

        AuthException exception = assertThrows(AuthException.class,
                () -> service.loginWithGoogle(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Google account email is missing or not verified", exception.getMessage());
    }

    @Test
    void loginWithGoogleRejectsExistingLocalEmail() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setCode("auth-code");
        request.setState("state-token");
        when(jwtTokenProvider.validateOAuthStateToken("state-token", "google")).thenReturn(true);
        when(authorizationCodeTokenResponseClient.getTokenResponse(any())).thenReturn(
                org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
                        .withToken("access")
                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                        .expiresIn(3600)
                        .additionalParameters(Map.of(OidcParameterNames.ID_TOKEN, "id-token"))
                        .build()
        );
        when(oidcIdTokenDecoderFactory.createDecoder(any(ClientRegistration.class))).thenReturn(jwtDecoder);
        when(jwtDecoder.decode("id-token")).thenReturn(googleJwt(
                "google-123",
                "user@example.com",
                "Google User",
                "https://avatar",
                true
        ));
        when(userRepository.findByProviderAndGoogleId(AuthProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user("u2", "user@example.com", AuthProvider.LOCAL)));

        AuthException exception = assertThrows(AuthException.class,
                () -> service.loginWithGoogle(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(
                "An account with this email already exists. Please login with email and password.",
                exception.getMessage()
        );
    }

    @Test
    void refreshTokenReturnsNewAccessTokenForExistingUser() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");
        User user = user("u1", "user@example.com", AuthProvider.LOCAL);

        when(jwtTokenProvider.validateToken("refresh-token", TokenType.REFRESH)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("refresh-token", TokenType.REFRESH)).thenReturn("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class))).thenReturn("new-access-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(30L);

        AuthResponse response = service.refreshToken(request);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("u1", response.getUser().getId());
    }

    @Test
    void refreshTokenRejectsInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad-token");
        when(jwtTokenProvider.validateToken("bad-token", TokenType.REFRESH)).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class,
                () -> service.refreshToken(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }

    @Test
    void refreshTokenRejectsNonRefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token");
        when(jwtTokenProvider.validateToken("token", TokenType.REFRESH)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("token")).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class,
                () -> service.refreshToken(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Token is not a refresh token", exception.getMessage());
    }

    @Test
    void getCurrentUserReturnsUserInfo() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user("u1", "user@example.com",
                AuthProvider.LOCAL)));

        assertEquals("user@example.com", service.getCurrentUser("u1").getEmail());
    }

    @Test
    void logoutRejectsInvalidRefreshToken() {
        when(jwtTokenProvider.validateToken("bad-token", TokenType.REFRESH)).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class,
                () -> service.logout("bad-token"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void logoutAcceptsValidRefreshToken() {
        when(jwtTokenProvider.validateToken("refresh-token", TokenType.REFRESH)).thenReturn(true);

        service.logout("refresh-token");

        verify(jwtTokenProvider).validateToken("refresh-token", TokenType.REFRESH);
    }

    private void mockTokenGeneration(User user) {
        when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class))).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(30L);
        assertNotNull(user.getRole());
    }

    private User user(String id, String email, AuthProvider provider) {
        return User.builder()
                .id(id)
                .email(email)
                .displayName("User")
                .avatarUrl("https://avatar")
                .provider(provider)
                .googleId(provider == AuthProvider.GOOGLE ? "google-123" : null)
                .role(Role.USER)
                .build();
    }

    private Jwt googleJwt(
            String subject,
            String email,
            String name,
            String picture,
            boolean emailVerified) {
        return Jwt.withTokenValue("id-token")
                .header("alg", "RS256")
                .subject(subject)
                .claim("email", email)
                .claim("name", name)
                .claim("picture", picture)
                .claim("email_verified", emailVerified)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
