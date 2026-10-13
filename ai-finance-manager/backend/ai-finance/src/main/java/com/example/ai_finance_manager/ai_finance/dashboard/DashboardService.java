package com.example.ai_finance_manager.ai_finance.dashboard;


import com.example.ai_finance_manager.ai_finance.Entity.Budget;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.Security.CurrentUserProvider;
import com.example.ai_finance_manager.ai_finance.common.MonthParser;
import com.example.ai_finance_manager.ai_finance.repository.BudgetRepository;
import com.example.ai_finance_manager.ai_finance.repository.ExpenseRepository;
import com.example.ai_finance_manager.ai_finance.repository.projection.CategoryTotal;
import com.example.ai_finance_manager.ai_finance.repository.projection.DailyTotal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal NEAR_THRESHOLD = BigDecimal.valueOf(80);

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String month) {
        User user = currentUserProvider.getCurrentUser();
        YearMonth yearMonth = MonthParser.parse(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        BigDecimal totalSpent = expenseRepository
                .sumByUserAndDateRange(user.getId(), start, end)
                .setScale(2, RoundingMode.HALF_UP);
        List<CategoryTotal> byCategory = expenseRepository.sumByCategory(user.getId(), start, end);
        List<DailyTotal> daily = zeroFill(yearMonth, expenseRepository.sumByDay(user.getId(), start, end));

        BigDecimal limit = budgetRepository
                .findByUserIdAndMonth(user.getId(), yearMonth.toString())
                .map(Budget::getLimitAmount)
                .orElse(null);

        BigDecimal percentUsed = null;
        BigDecimal remaining = null;
        DashboardStatus status = DashboardStatus.OK;

        if (limit != null && limit.signum() > 0) {
            percentUsed = totalSpent.multiply(HUNDRED).divide(limit, 2, RoundingMode.HALF_UP);
            remaining = limit.subtract(totalSpent);
            status = resolveStatus(totalSpent, limit);
        }

        return new DashboardResponse(totalSpent, limit, percentUsed, status, remaining, byCategory, daily);
    }

    private DashboardStatus resolveStatus(BigDecimal spent, BigDecimal limit) {
        if (spent.compareTo(limit) >= 0) {
            return DashboardStatus.OVER;
        }
        if (spent.multiply(HUNDRED).compareTo(limit.multiply(NEAR_THRESHOLD)) >= 0) {
            return DashboardStatus.NEAR;
        }
        return DashboardStatus.OK;
    }

    private List<DailyTotal> zeroFill(YearMonth yearMonth, List<DailyTotal> rows) {
        Map<LocalDate, BigDecimal> byDate = new HashMap<>();
        for (DailyTotal row : rows) {
            byDate.put(row.date(), row.total());
        }
        List<DailyTotal> result = new ArrayList<>(yearMonth.lengthOfMonth());
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            result.add(new DailyTotal(date, byDate.getOrDefault(date, ZERO)));
        }
        return result;
    }
}
