package com.example.ai_finance_manager.ai_finance.expense;


import com.example.ai_finance_manager.ai_finance.Entity.Expense;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.Security.CurrentUserProvider;
import com.example.ai_finance_manager.ai_finance.exception.BadRequestException;
import com.example.ai_finance_manager.ai_finance.exception.NotFoundException;
import com.example.ai_finance_manager.ai_finance.repository.ExpenseRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ExpenseService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final ExpenseRepository expenseRepository;
    private final CurrentUserProvider currentUserProvider;
    private final CategoryService categoryService;
    private final Counter expensesCreatedCounter;

    public ExpenseService(ExpenseRepository expenseRepository,
                          CurrentUserProvider currentUserProvider,
                          CategoryService categoryService,
                          MeterRegistry meterRegistry) {
        this.expenseRepository = expenseRepository;
        this.currentUserProvider = currentUserProvider;
        this.categoryService = categoryService;
        this.expensesCreatedCounter = Counter.builder("expenses.created")
                .description("Number of expenses created")
                .register(meterRegistry);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(String month) {
        User user = currentUserProvider.getCurrentUser();
        YearMonth yearMonth = parseMonth(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return expenseRepository.findByUserAndDateRange(user.getId(), start, end).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Expense expense = Expense.builder()
                .user(user)
                .amount(request.amount())
                .category(resolveCategory(request))
                .note(normalizeNote(request.note()))
                .date(request.date())
                .build();
        Expense saved = expenseRepository.save(expense);
        expensesCreatedCounter.increment();
        return ExpenseResponse.from(saved);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Expense expense = findOwned(id, user);
        expense.setAmount(request.amount());
        expense.setCategory(resolveCategory(request));
        expense.setNote(normalizeNote(request.note()));
        expense.setDate(request.date());
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long id) {
        User user = currentUserProvider.getCurrentUser();
        expenseRepository.delete(findOwned(id, user));
    }

    private Expense findOwned(Long id, User user) {
        return expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
    }

    private String resolveCategory(ExpenseRequest request) {
        String category = request.category();
        if (category != null && !category.isBlank()) {
            return category.trim();
        }
        String suggested = categoryService.suggest(request.note());
        return (suggested == null || suggested.isBlank())
                ? CategoryService.DEFAULT_CATEGORY
                : suggested.trim();
    }

    private String normalizeNote(String note) {
        return (note == null || note.isBlank()) ? null : note.trim();
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now(IST);
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("month must be in YYYY-MM format");
        }
    }
}