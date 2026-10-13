package com.example.ai_finance_manager.ai_finance.budget;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PutMapping
    public ResponseEntity<BudgetResponse> upsert(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.upsert(request));
    }
}
