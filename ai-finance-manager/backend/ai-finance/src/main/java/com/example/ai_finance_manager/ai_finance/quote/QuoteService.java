package com.example.ai_finance_manager.ai_finance.quote;

import com.example.ai_finance_manager.ai_finance.Entity.Quote;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.enums.QuoteGender;
import com.example.ai_finance_manager.ai_finance.exception.NotFoundException;
import com.example.ai_finance_manager.ai_finance.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;

    @Transactional(readOnly = true)
    public Quote forUser(User user) {
        List<QuoteGender> genders = user.getGender() == null
                ? List.of(QuoteGender.ANY)
                : List.of(QuoteGender.valueOf(user.getGender().name()), QuoteGender.ANY);

        long count = quoteRepository.countByGenderIn(genders);
        if (count == 0) {
            throw new NotFoundException("No quotes available");
        }

        int index = (int) ThreadLocalRandom.current().nextLong(count);
        return quoteRepository
                .findByGenderIn(genders, PageRequest.of(index, 1, Sort.by("id")))
                .getContent()
                .get(0);
    }
}
