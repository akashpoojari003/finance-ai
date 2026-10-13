package com.example.ai_finance_manager.ai_finance.quote;

import com.example.ai_finance_manager.ai_finance.Security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quote")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<QuoteResponse> get() {
        String text = quoteService.forUser(currentUserProvider.getCurrentUser()).getText();
        return ResponseEntity.ok(new QuoteResponse(text));
    }
}
