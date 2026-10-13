package com.example.ai_finance_manager.ai_finance.repository;

import com.example.ai_finance_manager.ai_finance.Entity.Expense;
import com.example.ai_finance_manager.ai_finance.repository.projection.CategoryTotal;
import com.example.ai_finance_manager.ai_finance.repository.projection.DailyTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            select e from Expense e
            where e.user.id = :userId
              and e.date between :startDate and :endDate
            order by e.date desc, e.id desc
            """)
    List<Expense> findByUserAndDateRange(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("""
            select sum(e.amount) from Expense e
            where e.user.id = :userId
              and e.date between :startDate and :endDate
            """)
    BigDecimal findSumByUserAndDateRange(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    default BigDecimal sumByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = findSumByUserAndDateRange(userId, startDate, endDate);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Query("""
            select new com.example.ai_finance_manager.ai_finance.repository.projection.CategoryTotal(
                e.category,
                sum(e.amount)
            )
            from Expense e
            where e.user.id = :userId
              and e.date between :startDate and :endDate
            group by e.category
            order by sum(e.amount) desc
            """)
    List<CategoryTotal> sumByCategory(@Param("userId") Long userId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    @Query("""
            select new com.example.ai_finance_manager.ai_finance.repository.projection.DailyTotal(
                e.date,
                sum(e.amount)
            )
            from Expense e
            where e.user.id = :userId
              and e.date between :startDate and :endDate
            group by e.date
            order by e.date asc
            """)
    List<DailyTotal> sumByDay(@Param("userId") Long userId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);
}