package com.example.Royal_Blueberry.service;

import com.example.Royal_Blueberry.dto.auth.*;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    GoogleLoginUrlResponse getGoogleLoginUrl();

    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserInfo getCurrentUser(String userId);
    void logout(String refreshToken);
}
