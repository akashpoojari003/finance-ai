package com.example.ai_finance_manager.ai_finance.profile;

import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.Security.CurrentUserProvider;
import com.example.ai_finance_manager.ai_finance.exception.BadRequestException;
import com.example.ai_finance_manager.ai_finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    public ProfileResponse get() {
        return ProfileResponse.from(currentUserProvider.getCurrentUser());
    }

    @Transactional
    public ProfileResponse update(ProfileRequest request) {
        User user = currentUserProvider.getCurrentUser();
        String phone = normalize(request.phone());

        if (request.whatsappOptIn() && phone == null) {
            throw new BadRequestException("Phone number is required to enable WhatsApp reminders");
        }

        user.setName(request.name().trim());
        user.setGender(request.gender());
        user.setPhone(phone);
        user.setWhatsappOptIn(request.whatsappOptIn());
        return ProfileResponse.from(userRepository.save(user));
    }

    private String normalize(String phone) {
        return (phone == null || phone.isBlank()) ? null : phone.trim();
    }
}
