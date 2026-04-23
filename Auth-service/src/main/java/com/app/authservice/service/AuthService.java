package com.app.authservice.service;

import java.util.List;

import com.app.authservice.dto.AuthResponse;
import com.app.authservice.dto.ChangePasswordRequest;
import com.app.authservice.dto.ChangeRoleRequest;
import com.app.authservice.dto.LoginRequest;
import com.app.authservice.dto.OAuthLoginRequest;
import com.app.authservice.dto.ProfileUpdateRequest;
import com.app.authservice.dto.RefreshTokenRequest;
import com.app.authservice.dto.RegisterRequest;
import com.app.authservice.dto.TokenValidationResponse;
import com.app.authservice.dto.UserProfileResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse oauthLogin(OAuthLoginRequest request);
    void logout();
    TokenValidationResponse validateToken(String token);
    AuthResponse refreshToken(RefreshTokenRequest request);
    UserProfileResponse getUserByEmail(String email);
    UserProfileResponse getUserById(Long userId);
    UserProfileResponse updateProfile(String currentEmail, ProfileUpdateRequest request);
    void changePassword(String currentEmail, ChangePasswordRequest request);
    List<UserProfileResponse> searchUsers(String keyword);
    void deactivateAccount(String currentEmail);
    UserProfileResponse updateRole(Long userId, ChangeRoleRequest request);
}
