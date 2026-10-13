package com.example.ai_finance_manager.ai_finance.common;


import com.example.ai_finance_manager.ai_finance.exception.BadRequestException;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class MonthParser {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private MonthParser() {
    }

    /** Blank or null returns the current month in Asia/Kolkata. */
    public static YearMonth parse(String month) {
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
