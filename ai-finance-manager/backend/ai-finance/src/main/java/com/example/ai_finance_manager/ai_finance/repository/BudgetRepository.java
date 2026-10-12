package com.example.ai_finance_manager.ai_finance.repository;


import com.example.ai_finance_manager.ai_finance.Entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("select b from Budget b where b.user.id = :userId and b.month = :month")
    Optional<Budget> findByUserIdAndMonth(@Param("userId") Long userId, @Param("month") String month);

    @Query("select b from Budget b join fetch b.user where b.month = :month")
    List<Budget> findAllByMonth(@Param("month") String month);
}