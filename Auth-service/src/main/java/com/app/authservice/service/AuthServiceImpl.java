package com.app.authservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.app.authservice.entity.AuthProvider;
import com.app.authservice.entity.Role;
import com.app.authservice.entity.User;
import com.app.authservice.repository.UserRepository;
import com.app.authservice.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(Role.READER)
                .provider(AuthProvider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated");
        }
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalArgumentException("Use OAuth login for this account");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse oauthLogin(OAuthLoginRequest request) {
        if (request.getProvider() == AuthProvider.LOCAL) {
            throw new IllegalArgumentException("Invalid OAuth provider");
        }
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseGet(() -> User.builder()
                        .email(request.getEmail().trim().toLowerCase())
                        .username(request.getUsername().trim())
                        .fullName(request.getFullName().trim())
                        .passwordHash("")
                        .role(Role.READER)
                        .provider(request.getProvider())
                        .avatarUrl(request.getAvatarUrl())
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build());

        if (user.getUserId() != null) {
            if (!user.isActive()) {
                throw new IllegalStateException("Account is deactivated");
            }
            if (user.getProvider() != request.getProvider()) {
                throw new IllegalArgumentException("Account is registered with a different provider");
            }
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
                user.setAvatarUrl(request.getAvatarUrl().trim());
            }
            user.setFullName(request.getFullName().trim());
        } else if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username already exists");
        }

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Override
    public void logout() {
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        boolean valid = jwtUtil.validateToken(token);
        if (!valid) {
            return TokenValidationResponse.builder().valid(false).build();
        }
        return TokenValidationResponse.builder()
                .valid(true)
                .email(jwtUtil.extractEmail(token))
                .role(jwtUtil.extractRole(token))
                .expiresAt(jwtUtil.extractExpiry(token).getTime())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtUtil.validateToken(request.getToken())) {
            throw new IllegalArgumentException("Invalid token");
        }
        String email = jwtUtil.extractEmail(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated");
        }
        return buildAuthResponse(user);
    }

    @Override
    public UserProfileResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toProfile(user);
    }

    @Override
    public UserProfileResponse getUserById(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toProfile(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String currentEmail, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(currentEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String nextEmail = request.getEmail().trim().toLowerCase();
        if (!nextEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(nextEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String nextUsername = request.getUsername().trim();
        if (!nextUsername.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsername(nextUsername)) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setEmail(nextEmail);
        user.setUsername(nextUsername);
        user.setFullName(request.getFullName().trim());
        user.setBio(request.getBio());
        user.setAvatarUrl(request.getAvatarUrl());
        user = userRepository.save(user);
        return toProfile(user);
    }

    @Override
    @Transactional
    public void changePassword(String currentEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(currentEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalStateException("Password change not allowed for OAuth account");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<UserProfileResponse> searchUsers(String keyword) {
        return userRepository.searchByUsername(keyword == null ? "" : keyword)
                .stream()
                .map(this::toProfile)
                .toList();
    }

    @Override
    @Transactional
    public void deactivateAccount(String currentEmail) {
        User user = userRepository.findByEmail(currentEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateRole(Long userId, ChangeRoleRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(request.getRole());
        user = userRepository.save(user);
        return toProfile(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresAt(jwtUtil.extractExpiry(token).getTime())
                .user(toProfile(user))
                .build();
    }

    private UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .active(user.isActive())
                .build();
    }
}
