package com.example.ai_finance_manager.ai_finance.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull
        @DecimalMin(value = "0.00", inclusive = false, message = "must be greater than 0")
        @Digits(integer = 13, fraction = 2)
        BigDecimal amount,

        @Size(max = 50) String category,

        @Size(max = 255) String note,

        @NotNull LocalDate date
) {
}
