package com.example.ai_finance_manager.ai_finance.alert;

import com.example.ai_finance_manager.ai_finance.Entity.Budget;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.enums.AlertLevel;
import com.example.ai_finance_manager.ai_finance.quote.QuoteService;
import com.example.ai_finance_manager.ai_finance.repository.BudgetRepository;
import com.example.ai_finance_manager.ai_finance.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal NEAR_THRESHOLD = BigDecimal.valueOf(80);
    private static final DateTimeFormatter MONTH_LABEL =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final QuoteService quoteService;
    private final NotificationService notificationService;

    /** Not transactional on purpose: one failing user must not roll back the others. */
    public AlertRunResult run() {
        YearMonth month = YearMonth.now(IST);
        List<Budget> budgets = budgetRepository.findAllByMonth(month.toString());

        int due = 0;
        int sent = 0;
        int failed = 0;

        for (Budget budget : budgets) {
            try {
                Optional<AlertMessage> message = evaluate(budget, month);
                if (message.isEmpty()) {
                    continue;
                }
                due++;
                if (notificationService.notify(message.get())) {
                    sent++;
                    budget.setLastAlertLevel(message.get().level());
                    budgetRepository.save(budget);
                } else {
                    failed++;
                }
            } catch (Exception ex) {
                failed++;
                log.error("Alert processing failed for budget {}", budget.getId(), ex);
            }
        }

        log.info("Alert run {}: checked={}, due={}, sent={}, failed={}",
                month, budgets.size(), due, sent, failed);
        return new AlertRunResult(budgets.size(), due, sent, failed);
    }

    private Optional<AlertMessage> evaluate(Budget budget, YearMonth month) {
        BigDecimal limit = budget.getLimitAmount();
        if (limit == null || limit.signum() <= 0) {
            return Optional.empty();
        }

        User user = budget.getUser();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        BigDecimal spent = expenseRepository.sumByUserAndDateRange(user.getId(), start, end);

        AlertLevel level;
        if (spent.compareTo(limit) >= 0) {
            level = AlertLevel.OVER;
        } else if (spent.multiply(HUNDRED).compareTo(limit.multiply(NEAR_THRESHOLD)) >= 0) {
            level = AlertLevel.NEAR;
        } else {
            return Optional.empty();
        }

        BigDecimal percent = spent.multiply(HUNDRED).divide(limit, 2, RoundingMode.HALF_UP);
        return Optional.of(new AlertMessage(
                user,
                month.format(MONTH_LABEL),
                spent.setScale(2, RoundingMode.HALF_UP),
                limit,
                percent,
                level,
                quoteFor(user)));
    }

    private String quoteFor(User user) {
        try {
            return quoteService.forUser(user).getText();
        } catch (Exception ex) {
            return null;
        }
    }
}
