package com.example.ai_finance_manager.ai_finance.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record BudgetRequest(
        @NotBlank
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "must be in YYYY-MM format")
        String month,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false, message = "must be greater than 0")
        @Digits(integer = 13, fraction = 2)
        BigDecimal limitAmount
) {
}
