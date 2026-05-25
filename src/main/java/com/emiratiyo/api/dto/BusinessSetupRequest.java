package com.emiratiyo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record BusinessSetupRequest(
    @NotBlank(message = "Full name is required")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Invalid mobile number")
    String mobileNumber,

    @NotBlank(message = "Country of residence is required")
    String countryOfResidence
) {}
