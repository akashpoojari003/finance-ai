package com.example.ai_finance_manager.ai_finance.dashboard;


import com.example.ai_finance_manager.ai_finance.Entity.Budget;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.Security.CurrentUserProvider;
import com.example.ai_finance_manager.ai_finance.repository.BudgetRepository;
import com.example.ai_finance_manager.ai_finance.repository.ExpenseRepository;
import com.example.ai_finance_manager.ai_finance.repository.projection.CategoryTotal;
import com.example.ai_finance_manager.ai_finance.repository.projection.DailyTotal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final String MONTH = "2026-10";
    private static final LocalDate START = LocalDate.of(2026, 10, 1);
    private static final LocalDate END = LocalDate.of(2026, 10, 31);
    private static final BigDecimal LIMIT = new BigDecimal("1000.00");

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private DashboardService dashboardService;
    private User user;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(expenseRepository, budgetRepository, currentUserProvider);
        user = User.builder().id(1L).name("Test").email("t@example.com").passwordHash("hash").build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
    }

    @ParameterizedTest(name = "spent {0} of 1000 -> {1}% {2}")
    @CsvSource({
            "790.00,  79.00,  OK",
            "800.00,  80.00,  NEAR",
            "999.00,  99.90,  NEAR",
            "1000.00, 100.00, OVER",
            "1250.50, 125.05, OVER"
    })
    void percentAndStatus_followThresholds(String spent, String percent, DashboardStatus status) {
        stubSpent(spent);
        stubBudget(LIMIT);

        DashboardResponse response = dashboardService.getDashboard(MONTH);

        assertThat(response.totalSpent()).isEqualByComparingTo(spent);
        assertThat(response.percentUsed()).isEqualByComparingTo(percent);
        assertThat(response.status()).isEqualTo(status);
        assertThat(response.remaining()).isEqualByComparingTo(LIMIT.subtract(new BigDecimal(spent)));
    }

    @Test
    void withoutBudget_limitPercentAndRemainingAreNullAndStatusIsOk() {
        stubSpent("500.00");
        when(budgetRepository.findByUserIdAndMonth(1L, MONTH)).thenReturn(Optional.empty());

        DashboardResponse response = dashboardService.getDashboard(MONTH);

        assertThat(response.limitAmount()).isNull();
        assertThat(response.percentUsed()).isNull();
        assertThat(response.remaining()).isNull();
        assertThat(response.status()).isEqualTo(DashboardStatus.OK);
    }

    @Test
    void daily_isZeroFilledForEveryDayOfTheMonth() {
        stubSpent("120.00");
        stubBudget(LIMIT);
        when(expenseRepository.sumByDay(1L, START, END))
                .thenReturn(List.of(new DailyTotal(LocalDate.of(2026, 10, 5), new BigDecimal("120.00"))));
        when(expenseRepository.sumByCategory(1L, START, END))
                .thenReturn(List.of(new CategoryTotal("Food", new BigDecimal("120.00"))));

        DashboardResponse response = dashboardService.getDashboard(MONTH);

        assertThat(response.daily()).hasSize(31);
        assertThat(response.daily().get(0).date()).isEqualTo(START);
        assertThat(response.daily().get(30).date()).isEqualTo(END);
        assertThat(response.daily().get(0).total()).isEqualByComparingTo("0");
        assertThat(response.daily().get(4).total()).isEqualByComparingTo("120.00");
        assertThat(response.daily().get(5).total()).isEqualByComparingTo("0");
        assertThat(response.byCategory()).containsExactly(new CategoryTotal("Food", new BigDecimal("120.00")));
    }

    private void stubSpent(String amount) {
        when(expenseRepository.sumByUserAndDateRange(1L, START, END)).thenReturn(new BigDecimal(amount));
    }

    private void stubBudget(BigDecimal limit) {
        Budget budget = Budget.builder().id(5L).user(user).month(MONTH).limitAmount(limit).build();
        when(budgetRepository.findByUserIdAndMonth(1L, MONTH)).thenReturn(Optional.of(budget));
    }
}
