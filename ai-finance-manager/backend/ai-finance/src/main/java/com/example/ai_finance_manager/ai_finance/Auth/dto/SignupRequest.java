package com.example.ai_finance_manager.ai_finance.Auth.dto;

import com.example.ai_finance_manager.ai_finance.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Gender gender,
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "must be 10-15 digits, optional leading +") String phone
) {
}
