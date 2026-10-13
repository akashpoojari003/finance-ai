package com.example.ai_finance_manager.ai_finance.alert;

import com.example.ai_finance_manager.ai_finance.Entity.Budget;
import com.example.ai_finance_manager.ai_finance.Entity.Quote;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.enums.AlertLevel;
import com.example.ai_finance_manager.ai_finance.enums.QuoteGender;
import com.example.ai_finance_manager.ai_finance.quote.QuoteService;
import com.example.ai_finance_manager.ai_finance.repository.BudgetRepository;
import com.example.ai_finance_manager.ai_finance.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertSchedulerTest {

    private static final BigDecimal LIMIT = new BigDecimal("1000.00");

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private QuoteService quoteService;
    @Mock
    private NotificationService notificationService;

    private AlertService alertService;
    private AlertScheduler scheduler;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(budgetRepository, expenseRepository, quoteService, notificationService);
        scheduler = new AlertScheduler(alertService);
        lenient().when(quoteService.forUser(any(User.class)))
                .thenReturn(Quote.builder().gender(QuoteGender.ANY).text("Save first.").build());
        lenient().when(notificationService.notify(any(AlertMessage.class))).thenReturn(true);
    }

    @Test
    void dailyCheck_sendsOnlyAtOrAboveEightyPercent() {
        Budget b1 = budgetFor(1L, LIMIT);
        Budget b2 = budgetFor(2L, LIMIT);
        Budget b3 = budgetFor(3L, LIMIT);
        Budget b4 = budgetFor(4L, LIMIT);
        budgets(b1, b2, b3, b4);
        spent(b1, "790.00");
        spent(b2, "800.00");
        spent(b3, "1000.00");
        spent(b4, "1500.00");

        scheduler.dailyCheck();

        ArgumentCaptor<AlertMessage> captor = ArgumentCaptor.forClass(AlertMessage.class);
        verify(notificationService, times(3)).notify(captor.capture());
        List<AlertMessage> sent = captor.getAllValues();
        assertThat(sent).extracting(m -> m.user().getId()).containsExactly(2L, 3L, 4L);
        assertThat(sent).extracting(AlertMessage::level)
                .containsExactly(AlertLevel.NEAR, AlertLevel.OVER, AlertLevel.OVER);
        verify(budgetRepository, never()).save(b1);
    }

    @Test
    void dailyCheck_doesNotSendJustBelowEightyPercent() {
        Budget budget = budgetFor(1L, LIMIT);
        budgets(budget);
        spent(budget, "799.99");

        scheduler.dailyCheck();

        verify(notificationService, never()).notify(any(AlertMessage.class));
    }

    @Test
    void dailyCheck_sendsAgainOnEveryRunWhileAboveThreshold() {
        Budget budget = budgetFor(1L, LIMIT);
        budgets(budget);
        spent(budget, "900.00");

        scheduler.dailyCheck();
        scheduler.dailyCheck();

        verify(notificationService, times(2)).notify(any(AlertMessage.class));
    }

    @Test
    void dailyCheck_skipsBudgetsWithoutUsableLimit() {
        budgets(budgetFor(1L, null), budgetFor(2L, BigDecimal.ZERO));

        scheduler.dailyCheck();

        verify(expenseRepository, never()).sumByUserAndDateRange(any(), any(), any());
        verify(notificationService, never()).notify(any(AlertMessage.class));
    }

    @Test
    void run_recordsAlertLevelOnBudgetAfterSuccessfulSend() {
        Budget budget = budgetFor(1L, LIMIT);
        budgets(budget);
        spent(budget, "1200.00");

        AlertRunResult result = alertService.run();

        assertThat(result.checked()).isEqualTo(1);
        assertThat(result.due()).isEqualTo(1);
        assertThat(result.sent()).isEqualTo(1);
        assertThat(result.failed()).isZero();
        assertThat(budget.getLastAlertLevel()).isEqualTo(AlertLevel.OVER);
        verify(budgetRepository).save(budget);
    }

    @Test
    void run_countsFailedDeliveriesAndDoesNotSaveBudget() {
        Budget budget = budgetFor(1L, LIMIT);
        budgets(budget);
        spent(budget, "900.00");
        when(notificationService.notify(any(AlertMessage.class))).thenReturn(false);

        AlertRunResult result = alertService.run();

        assertThat(result.due()).isEqualTo(1);
        assertThat(result.sent()).isZero();
        assertThat(result.failed()).isEqualTo(1);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void run_oneFailingUserDoesNotBlockOthers() {
        Budget b2 = budgetFor(2L, LIMIT);
        Budget b3 = budgetFor(3L, LIMIT);
        budgets(b2, b3);
        spent(b2, "900.00");
        spent(b3, "900.00");
        when(notificationService.notify(argThat((AlertMessage m) -> m.user().getId() == 2L)))
                .thenThrow(new IllegalStateException("smtp down"));

        AlertRunResult result = alertService.run();

        assertThat(result.due()).isEqualTo(2);
        assertThat(result.sent()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        verify(budgetRepository).save(b3);
        verify(budgetRepository, never()).save(b2);
    }

    private Budget budgetFor(long userId, BigDecimal limit) {
        User user = User.builder()
                .id(userId)
                .name("User " + userId)
                .email("u" + userId + "@example.com")
                .passwordHash("hash")
                .build();
        return Budget.builder().id(userId * 10).user(user).month("2026-10").limitAmount(limit).build();
    }

    private void budgets(Budget... budgets) {
        when(budgetRepository.findAllByMonth(anyString())).thenReturn(List.of(budgets));
    }

    private void spent(Budget budget, String amount) {
        when(expenseRepository.sumByUserAndDateRange(
                eq(budget.getUser().getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal(amount));
    }
}
