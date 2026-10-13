package com.example.ai_finance_manager.ai_finance.alert;


import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.enums.AlertLevel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public record AlertMessage(
        User user,
        String monthLabel,
        BigDecimal spent,
        BigDecimal limit,
        BigDecimal percent,
        AlertLevel level,
        String quote
) {

    public String statusLabel() {
        return level == AlertLevel.OVER ? "Over budget" : "Near limit";
    }

    public String statusColor() {
        return level == AlertLevel.OVER ? "#dc2626" : "#f59e0b";
    }

    public static String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.of("en", "IN")).format(value);
    }
}
