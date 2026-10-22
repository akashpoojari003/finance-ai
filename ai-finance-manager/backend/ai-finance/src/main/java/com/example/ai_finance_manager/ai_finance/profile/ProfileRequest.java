package com.example.ai_finance_manager.ai_finance.profile;

import com.example.ai_finance_manager.ai_finance.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Gender gender,
        @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "must be in E.164 format, e.g. +919876543210")
        String phone,
        boolean whatsappOptIn
) {
}
