package com.emiratiyo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record ContactRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Invalid phone number")
    String phone,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    String message
) {}
