package com.example.ai_finance_manager.ai_finance.budget;

import com.example.ai_finance_manager.ai_finance.Entity.Budget;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.Security.CurrentUserProvider;
import com.example.ai_finance_manager.ai_finance.common.MonthParser;
import com.example.ai_finance_manager.ai_finance.enums.AlertLevel;
import com.example.ai_finance_manager.ai_finance.exception.ConflictException;
import com.example.ai_finance_manager.ai_finance.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public BudgetResponse upsert(BudgetRequest request) {
        User user = currentUserProvider.getCurrentUser();
        String month = MonthParser.parse(request.month()).toString();

        Budget budget = budgetRepository.findByUserIdAndMonth(user.getId(), month)
                .orElseGet(() -> Budget.builder().user(user).month(month).build());

        boolean limitChanged = budget.getId() == null
                || budget.getLimitAmount().compareTo(request.limitAmount()) != 0;
        if (limitChanged) {
            budget.setLastAlertLevel(AlertLevel.NONE);
        }
        budget.setLimitAmount(request.limitAmount());

        try {
            return BudgetResponse.from(budgetRepository.save(budget));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Budget was modified concurrently, please retry");
        }
    }

    @Transactional(readOnly = true)
    public BudgetResponse get(String month) {
        User user = currentUserProvider.getCurrentUser();
        String yearMonth = MonthParser.parse(month).toString();
        return budgetRepository.findByUserIdAndMonth(user.getId(), yearMonth)
                .map(BudgetResponse::from)
                .orElse(new BudgetResponse(yearMonth, null));
    }
}
