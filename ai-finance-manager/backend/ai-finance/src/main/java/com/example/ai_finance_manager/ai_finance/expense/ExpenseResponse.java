package com.example.ai_finance_manager.ai_finance.expense;

import com.example.ai_finance_manager.ai_finance.Entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        String category,
        String note,
        LocalDate date,
        LocalDateTime createdAt
) {

    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getNote(),
                expense.getDate(),
                expense.getCreatedAt());
    }
}