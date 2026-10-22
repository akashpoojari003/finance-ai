package com.example.ai_finance_manager.ai_finance.profile;

import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.enums.Gender;

public record ProfileResponse(
        String name,
        String email,
        Gender gender,
        String phone,
        boolean whatsappOptIn
) {

    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getName(),
                user.getEmail(),
                user.getGender(),
                user.getPhone(),
                user.isWhatsappOptIn());
    }
}
