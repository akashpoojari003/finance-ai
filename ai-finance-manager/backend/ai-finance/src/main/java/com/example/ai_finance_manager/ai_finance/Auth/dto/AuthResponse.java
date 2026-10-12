package com.example.ai_finance_manager.ai_finance.Auth.dto;


import com.example.ai_finance_manager.ai_finance.enums.Gender;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String name,
        Gender gender
) {
}
