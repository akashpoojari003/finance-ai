package com.example.ai_finance_manager.ai_finance.repository.projection;


import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTotal(LocalDate date, BigDecimal total) {
}
