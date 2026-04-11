package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.auth.AuthResponse;
import com.example.Royal_Blueberry.dto.auth.GoogleLoginRequest;
import com.example.Royal_Blueberry.dto.auth.GoogleLoginUrlResponse;
import com.example.Royal_Blueberry.dto.auth.LoginRequest;
import com.example.Royal_Blueberry.dto.auth.RefreshTokenRequest;
import com.example.Royal_Blueberry.dto.auth.RegisterRequest;
import com.example.Royal_Blueberry.dto.auth.UserInfo;
import com.example.Royal_Blueberry.entity.User;
import com.example.Royal_Blueberry.exception.AuthException;
import com.example.Royal_Blueberry.repository.UserRepository;
import com.example.Royal_Blueberry.security.CustomUserDetails;
import com.example.Royal_Blueberry.security.JwtTokenProvider;
import com.example.Royal_Blueberry.service.AuthService;
import com.example.Royal_Blueberry.util.AuthProvider;
import com.example.Royal_Blueberry.util.Role;
import com.example.Royal_Blueberry.util.TokenType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final String GOOGLE_PROVIDER = "google";
    private static final String[] GOOGLE_SCOPES = {"openid", "email", "profile"};

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
            authorizationCodeTokenResponseClient;
    private final JwtDecoderFactory<ClientRegistration> oidcIdTokenDecoderFactory;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String googleRedirectUri;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager,
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
                    authorizationCodeTokenResponseClient,
            JwtDecoderFactory<ClientRegistration> oidcIdTokenDecoderFactory,
            @Value("${google.client-id:}") String googleClientId,
            @Value("${google.client-secret:}") String googleClientSecret,
            @Value("${google.redirect-uri:}") String googleRedirectUri) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.authorizationCodeTokenResponseClient = authorizationCodeTokenResponseClient;
        this.oidcIdTokenDecoderFactory = oidcIdTokenDecoderFactory;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.googleRedirectUri = googleRedirectUri;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("[AuthService] Login attempt - email={}", normalizedEmail);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail, request.getPassword()));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("[AuthService] Login successful - userId={}, email={}",
                    userDetails.getUser().getId(), normalizedEmail);
            return buildAuthResponse(userDetails);
        } catch (BadCredentialsException e) {
            log.warn("[AuthService] Login failed - invalid credentials for email={}", normalizedEmail);
            throw new AuthException("Invalid email or password");
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("[AuthService] Registration attempt - email={}, displayName={}",
                normalizedEmail, request.getDisplayName());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.warn("[AuthService] Registration failed - email already exists: {}", normalizedEmail);
            throw new AuthException("Email already exists", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .password(passwordEncoder.encode(request.getPassword()))
                .email(normalizedEmail)
                .displayName(request.getDisplayName())
                .build();

        user = saveUserOrThrowConflict(user, "Email already exists");
        log.info("[AuthService] Registration successful - userId={}, email={}",
                user.getId(), normalizedEmail);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return buildAuthResponse(userDetails);
    }

    @Override
    public GoogleLoginUrlResponse getGoogleLoginUrl() {
        log.info("[AuthService] Generating Google login URL");
        assertGoogleLoginConfigured();

        String state = jwtTokenProvider.generateOAuthStateToken(GOOGLE_PROVIDER);
        ClientRegistration googleClientRegistration = getGoogleClientRegistration();
        String url = buildGoogleAuthorizationRequest(googleClientRegistration, state)
                .getAuthorizationRequestUri();

        log.debug("[AuthService] Google login URL generated - redirectUri={}",
                googleClientRegistration.getRedirectUri());

        return GoogleLoginUrlResponse.builder()
                .url(url)
                .state(state)
                .redirectUri(googleClientRegistration.getRedirectUri())
                .scope(formatScopes(googleClientRegistration))
                .build();
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        log.info("[AuthService] Google OAuth login attempt");
        assertGoogleLoginConfigured();
        validateGoogleState(request.getState());

        ClientRegistration googleClientRegistration = getGoogleClientRegistration();
        OAuth2AccessTokenResponse tokenResponse = exchangeGoogleAuthorizationCode(
                googleClientRegistration, request.getCode(), request.getState());
        Jwt idToken = decodeGoogleIdToken(googleClientRegistration, tokenResponse);

        String googleId = idToken.getSubject();
        String email = normalizeEmail(idToken.getClaimAsString("email"));
        String name = idToken.getClaimAsString("name");
        String pictureUrl = idToken.getClaimAsString("picture");
        Boolean emailVerified = idToken.getClaimAsBoolean("email_verified");

        log.info("[AuthService] Google token decoded - email={}, name={}, emailVerified={}",
                email, name, emailVerified);

        if (!StringUtils.hasText(email) || !Boolean.TRUE.equals(emailVerified)) {
            log.warn("[AuthService] Google login rejected - email missing or not verified");
            throw new AuthException("Google account email is missing or not verified",
                    HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByProviderAndGoogleId(AuthProvider.GOOGLE, googleId)
                .orElseGet(() -> {
                    userRepository.findByEmailIgnoreCase(email).ifPresent(existingUser -> {
                        log.warn("[AuthService] Google login conflict - email {} already has local account", email);
                        throw new AuthException(
                                "An account with this email already exists. Please login with email and password.",
                                HttpStatus.CONFLICT);
                    });

                    User newUser = User.builder()
                            .email(email)
                            .displayName(name)
                            .avatarUrl(pictureUrl)
                            .provider(AuthProvider.GOOGLE)
                            .googleId(googleId)
                            .role(Role.USER)
                            .build();

                    log.info("[AuthService] New Google user created: {}", email);
                    return saveUserOrThrowConflict(
                            newUser,
                            "An account with this email already exists. Please login with email and password."
                    );
                });

        user.setDisplayName(name);
        user.setAvatarUrl(pictureUrl);
        userRepository.save(user);

        log.info("[AuthService] Google login successful - userId={}, email={}", user.getId(), email);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return buildAuthResponse(userDetails);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        log.info("[AuthService] Token refresh attempt");

        if (!jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH)) {
            log.warn("[AuthService] Token refresh failed - invalid or expired refresh token");
            throw new AuthException("Invalid or expired refresh token");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.warn("[AuthService] Token refresh failed - token is not a refresh token");
            throw new AuthException("Token is not a refresh token", HttpStatus.BAD_REQUEST);
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken, TokenType.REFRESH);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[AuthService] Token refresh failed - user not found: {}", userId);
                    return new AuthException("User not found", HttpStatus.NOT_FOUND);
                });

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        log.info("[AuthService] Token refreshed - userId={}", userId);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .user(mapToUserInfo(user))
                .build();
    }

    @Override
    public UserInfo getCurrentUser(String userId) {
        log.debug("[AuthService] Fetching current user - userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[AuthService] User not found - userId={}", userId);
                    return new AuthException("User not found", HttpStatus.NOT_FOUND);
                });
        return mapToUserInfo(user);
    }

    private AuthResponse buildAuthResponse(CustomUserDetails userDetails) {
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .user(mapToUserInfo(userDetails.getUser()))
                .build();
    }

    private UserInfo mapToUserInfo(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();
    }

    private void assertGoogleLoginConfigured() {
        if (!StringUtils.hasText(googleClientId)
                || !StringUtils.hasText(googleClientSecret)
                || !StringUtils.hasText(googleRedirectUri)) {
            throw new AuthException("Google login is not configured on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateGoogleState(String state) {
        if (!jwtTokenProvider.validateOAuthStateToken(state, GOOGLE_PROVIDER)) {
            throw new AuthException("Invalid Google login state", HttpStatus.UNAUTHORIZED);
        }
    }

    private OAuth2AccessTokenResponse exchangeGoogleAuthorizationCode(
            ClientRegistration googleClientRegistration,
            String authorizationCode,
            String state) {
        OAuth2AuthorizationRequest authorizationRequest = buildGoogleAuthorizationRequest(
                googleClientRegistration, state);
        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse
                .success(authorizationCode)
                .redirectUri(googleClientRegistration.getRedirectUri())
                .state(state)
                .build();
        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                googleClientRegistration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
        );
        try {
            OAuth2AccessTokenResponse tokenResponse =
                    authorizationCodeTokenResponseClient.getTokenResponse(grantRequest);
            if (!StringUtils.hasText(getIdTokenValue(tokenResponse))) {
                throw new AuthException(
                        "Google token exchange did not return an ID token. Ensure the client requested openid email profile scopes.",
                        HttpStatus.UNAUTHORIZED
                );
            }
            return tokenResponse;
        } catch (OAuth2AuthorizationException ex) {
            log.warn("Google token exchange failed: errorCode={}, description={}",
                    ex.getError().getErrorCode(), ex.getError().getDescription());
            throw new AuthException("Invalid Google authorization code", HttpStatus.UNAUTHORIZED);
        }
    }

    private Jwt decodeGoogleIdToken(
            ClientRegistration googleClientRegistration,
            OAuth2AccessTokenResponse tokenResponse) {
        try {
            return oidcIdTokenDecoderFactory.createDecoder(googleClientRegistration)
                    .decode(getIdTokenValue(tokenResponse));
        } catch (JwtException ex) {
            log.error("Failed to verify Google ID token", ex);
            throw new AuthException("Invalid Google ID token", HttpStatus.UNAUTHORIZED);
        }
    }

    private OAuth2AuthorizationRequest buildGoogleAuthorizationRequest(
            ClientRegistration googleClientRegistration,
            String state) {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(googleClientRegistration.getProviderDetails().getAuthorizationUri())
                .clientId(googleClientRegistration.getClientId())
                .redirectUri(googleClientRegistration.getRedirectUri())
                .scopes(googleClientRegistration.getScopes())
                .state(state)
                .build();
    }

    private ClientRegistration getGoogleClientRegistration() {
        return CommonOAuth2Provider.GOOGLE.getBuilder(GOOGLE_PROVIDER)
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .redirectUri(googleRedirectUri)
                .scope(GOOGLE_SCOPES)
                .build();
    }

    private String getIdTokenValue(OAuth2AccessTokenResponse tokenResponse) {
        Object idToken = tokenResponse.getAdditionalParameters().get(OidcParameterNames.ID_TOKEN);
        return idToken instanceof String ? (String) idToken : null;
    }

    private String formatScopes(ClientRegistration googleClientRegistration) {
        return googleClientRegistration.getScopes()
                .stream()
                .collect(Collectors.joining(" "));
    }

    private User saveUserOrThrowConflict(User user, String duplicateMessage) {
        try {
            return userRepository.save(user);
        } catch (DuplicateKeyException ex) {
            throw new AuthException(duplicateMessage, HttpStatus.CONFLICT);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
