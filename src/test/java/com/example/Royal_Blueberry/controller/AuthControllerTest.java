package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.auth.AuthResponse;
import com.example.Royal_Blueberry.dto.auth.GoogleLoginRequest;
import com.example.Royal_Blueberry.dto.auth.GoogleLoginUrlResponse;
import com.example.Royal_Blueberry.dto.auth.LoginRequest;
import com.example.Royal_Blueberry.dto.auth.RefreshTokenRequest;
import com.example.Royal_Blueberry.dto.auth.UserInfo;
import com.example.Royal_Blueberry.exception.GlobalExceptionHandler;
import com.example.Royal_Blueberry.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getGoogleLoginUrlReturnsServiceResponse() throws Exception {
        when(authService.getGoogleLoginUrl()).thenReturn(GoogleLoginUrlResponse.builder()
                .url("https://accounts.google.com")
                .state("state-1")
                .redirectUri("http://localhost/callback")
                .scope("openid email profile")
                .build());

        mockMvc.perform(get("/api/auth/google/url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("state-1")))
                .andExpect(jsonPath("$.redirectUri", is("http://localhost/callback")));
    }

    @Test
    void loginReturnsAuthPayload() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.user.id", is("u1")));
    }

    @Test
    void registerValidationErrorsAreHandledByAdvice() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bad-email","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.email", is("Email must be valid")))
                .andExpect(jsonPath("$.errors.password",
                        is("Password must be between 6 and 100 characters")));
    }

    @Test
    void googleLoginReturnsAuthPayload() throws Exception {
        when(authService.loginWithGoogle(any(GoogleLoginRequest.class))).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"auth-code","state":"state-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is("user@example.com")));
    }

    @Test
    void refreshTokenReturnsAuthPayload() throws Exception {
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")));
    }

    private AuthResponse authResponse() {
        return AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(30L)
                .user(UserInfo.builder()
                        .id("u1")
                        .email("user@example.com")
                        .displayName("User")
                        .role("USER")
                        .build())
                .build();
    }
}
