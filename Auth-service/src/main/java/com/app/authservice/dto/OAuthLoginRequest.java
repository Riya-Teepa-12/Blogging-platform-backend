package com.app.authservice.dto;

import com.app.authservice.entity.AuthProvider;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OAuthLoginRequest {
    @NotNull
    private AuthProvider provider;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 120)
    private String fullName;

    @NotBlank
    @Size(min = 3, max = 80)
    private String username;

    @Size(max = 512)
    private String avatarUrl;
}
