package com.example.ai_finance_manager.ai_finance.dashboard;


import com.example.ai_finance_manager.ai_finance.repository.projection.CategoryTotal;
import com.example.ai_finance_manager.ai_finance.repository.projection.DailyTotal;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalSpent,
        BigDecimal limitAmount,
        BigDecimal percentUsed,
        DashboardStatus status,
        BigDecimal remaining,
        List<CategoryTotal> byCategory,
        List<DailyTotal> daily
) {
}