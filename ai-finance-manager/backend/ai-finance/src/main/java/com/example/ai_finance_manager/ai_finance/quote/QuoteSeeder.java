package com.example.ai_finance_manager.ai_finance.quote;


import com.example.ai_finance_manager.ai_finance.Entity.Quote;
import com.example.ai_finance_manager.ai_finance.enums.QuoteGender;
import com.example.ai_finance_manager.ai_finance.repository.QuoteRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteSeeder implements CommandLineRunner {

    private static final String RESOURCE = "quotes.json";

    private final QuoteRepository quoteRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) {
        if (quoteRepository.count() > 0) {
            return;
        }

        Map<String, List<String>> data;
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            data = objectMapper.readValue(in, new TypeReference<Map<String, List<String>>>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read " + RESOURCE, ex);
        }

        List<Quote> quotes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            QuoteGender gender;
            try {
                gender = QuoteGender.valueOf(entry.getKey().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                log.warn("Skipping unknown quote group: {}", entry.getKey());
                continue;
            }
            for (String text : entry.getValue()) {
                if (text != null && !text.isBlank()) {
                    quotes.add(Quote.builder().gender(gender).text(text.trim()).build());
                }
            }
        }

        quoteRepository.saveAll(quotes);
        log.info("Seeded {} quotes", quotes.size());
    }
}
