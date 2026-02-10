package com.thecheatschool.thecheatschool.server.model.tcs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Contact form submission request for TheCheatSchool registration")
public class TCSContactRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotBlank(message = "College/University is required")
    private String college;

    @NotBlank(message = "Year of study is required")
    private String yearOfStudy;

    @NotBlank(message = "Branch is required")
    private String branch;

    @NotBlank(message = "Please tell us how you heard about us")
    private String hearAboutUs;

    private String hearAboutUsOther;
}