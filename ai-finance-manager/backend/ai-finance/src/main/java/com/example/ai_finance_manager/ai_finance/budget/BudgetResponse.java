package com.example.ai_finance_manager.ai_finance.budget;


import com.example.ai_finance_manager.ai_finance.Entity.Budget;

import java.math.BigDecimal;

public record BudgetResponse(String month, BigDecimal limitAmount) {

    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(budget.getMonth(), budget.getLimitAmount());
    }
}