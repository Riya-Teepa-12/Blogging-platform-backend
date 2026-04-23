package com.app.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank
    @Size(min = 3, max = 80)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 120)
    private String fullName;

    @Size(max = 2000)
    private String bio;

    @Size(max = 512)
    private String avatarUrl;
}
