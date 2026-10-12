package com.example.ai_finance_manager.ai_finance.repository;

import com.example.ai_finance_manager.ai_finance.Entity.Quote;
import com.example.ai_finance_manager.ai_finance.enums.Gender;
import com.example.ai_finance_manager.ai_finance.enums.QuoteGender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByGenderIn(Collection<QuoteGender> genders);

    default List<Quote> findByUserGenderOrAny(Gender gender) {
        QuoteGender target = gender == null ? QuoteGender.ANY : QuoteGender.valueOf(gender.name());
        return findByGenderIn(List.of(target, QuoteGender.ANY));
    }
}
