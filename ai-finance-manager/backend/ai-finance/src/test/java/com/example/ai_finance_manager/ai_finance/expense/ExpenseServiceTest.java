package com.example.ai_finance_manager.ai_finance.expense;

import com.example.ai_finance_manager.ai_finance.Entity.Expense;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.Security.CurrentUserProvider;
import com.example.ai_finance_manager.ai_finance.exception.BadRequestException;
import com.example.ai_finance_manager.ai_finance.exception.NotFoundException;
import com.example.ai_finance_manager.ai_finance.repository.ExpenseRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private CategoryService categoryService;

    private SimpleMeterRegistry meterRegistry;
    private ExpenseService expenseService;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        expenseService = new ExpenseService(expenseRepository, currentUserProvider, categoryService, meterRegistry);
        userA = user(1L, "a@example.com");
        userB = user(2L, "b@example.com");
        lenient().when(currentUserProvider.getCurrentUser()).thenReturn(userA);
    }

    @Test
    void list_queriesOnlyTheCurrentUsersExpenses() {
        Expense mine = expense(10L, userA);
        when(expenseRepository.findByUserAndDateRange(1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(mine));

        List<ExpenseResponse> result = expenseService.list("2026-10");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
        verify(expenseRepository, never()).findByUserAndDateRange(eq(2L), any(), any());
    }

    @Test
    void list_withInvalidMonth_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> expenseService.list("2026-13"));
    }

    @Test
    void update_expenseOwnedByAnotherUser_throwsNotFoundAndSavesNothing() {
        when(expenseRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseService.update(10L, request("Food")));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void delete_expenseOwnedByAnotherUser_throwsNotFoundAndDeletesNothing() {
        when(expenseRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseService.delete(10L));

        verify(expenseRepository, never()).delete(any(Expense.class));
    }

    @Test
    void update_ownedExpense_updatesAllFields() {
        Expense existing = expense(10L, userA);
        when(expenseRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseResponse response = expenseService.update(10L,
                new ExpenseRequest(new BigDecimal("250.00"), "Travel", "metro", LocalDate.of(2026, 10, 14)));

        assertThat(response.amount()).isEqualByComparingTo("250.00");
        assertThat(response.category()).isEqualTo("Travel");
        assertThat(response.note()).isEqualTo("metro");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 10, 14));
    }

    @Test
    void delete_ownedExpense_deletesIt() {
        Expense existing = expense(10L, userA);
        when(expenseRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));

        expenseService.delete(10L);

        verify(expenseRepository).delete(existing);
    }

    @Test
    void create_attachesCurrentUserAndIncrementsCounter() {
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        expenseService.create(request("Food"));

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(userA);
        assertThat(meterRegistry.get("expenses.created").counter().count()).isEqualTo(1.0);
    }

    @Test
    void create_withBlankCategory_usesSuggestedCategory() {
        when(categoryService.suggest("uber ride")).thenReturn("Travel");
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseResponse response = expenseService.create(
                new ExpenseRequest(new BigDecimal("120.00"), "  ", "uber ride", LocalDate.of(2026, 10, 13)));

        assertThat(response.category()).isEqualTo("Travel");
    }

    @Test
    void create_withBlankCategoryAndNoSuggestion_fallsBackToOther() {
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseResponse response = expenseService.create(
                new ExpenseRequest(new BigDecimal("120.00"), null, null, LocalDate.of(2026, 10, 13)));

        assertThat(response.category()).isEqualTo("Other");
    }

    private static ExpenseRequest request(String category) {
        return new ExpenseRequest(new BigDecimal("100.00"), category, "lunch", LocalDate.of(2026, 10, 13));
    }

    private static User user(long id, String email) {
        return User.builder().id(id).name("User " + id).email(email).passwordHash("hash").build();
    }

    private static Expense expense(long id, User owner) {
        return Expense.builder()
                .id(id)
                .user(owner)
                .amount(new BigDecimal("100.00"))
                .category("Food")
                .note("lunch")
                .date(LocalDate.of(2026, 10, 13))
                .build();
    }
}