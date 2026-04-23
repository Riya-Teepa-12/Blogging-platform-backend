package com.app.authservice.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.authservice.dto.AuthResponse;
import com.app.authservice.dto.ChangePasswordRequest;
import com.app.authservice.dto.ChangeRoleRequest;
import com.app.authservice.dto.LoginRequest;
import com.app.authservice.dto.OAuthLoginRequest;
import com.app.authservice.dto.ProfileUpdateRequest;
import com.app.authservice.dto.RefreshTokenRequest;
import com.app.authservice.dto.RegisterRequest;
import com.app.authservice.dto.SimpleApiResponse;
import com.app.authservice.dto.TokenValidationResponse;
import com.app.authservice.dto.UserProfileResponse;
import com.app.authservice.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/oauth/login")
    public AuthResponse oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        return authService.oauthLogin(request);
    }

    @PostMapping("/logout")
    public SimpleApiResponse logout() {
        authService.logout();
        return new SimpleApiResponse("Logged out");
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @GetMapping("/validate")
    public TokenValidationResponse validateToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {
        String token = tokenParam;
        if (token == null && authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        if (token == null || token.isBlank()) {
            return TokenValidationResponse.builder().valid(false).build();
        }
        return authService.validateToken(token);
    }

    @GetMapping("/profile")
    public UserProfileResponse getProfile(Authentication authentication) {
        return authService.getUserByEmail(authentication.getName());
    }

    @PutMapping("/profile")
    public UserProfileResponse updateProfile(Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(authentication.getName(), request);
    }

    @PutMapping("/password")
    public SimpleApiResponse changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return new SimpleApiResponse("Password updated");
    }

    @PatchMapping("/deactivate")
    public SimpleApiResponse deactivateAccount(Authentication authentication) {
        authService.deactivateAccount(authentication.getName());
        return new SimpleApiResponse("Account deactivated");
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileResponse> searchUsers(@RequestParam(value = "q", defaultValue = "") String keyword) {
        return authService.searchUsers(keyword);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserProfileResponse getUserById(@PathVariable Long userId) {
        return authService.getUserById(userId);
    }

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserProfileResponse updateRole(@PathVariable Long userId, @Valid @RequestBody ChangeRoleRequest request) {
        return authService.updateRole(userId, request);
    }
}
