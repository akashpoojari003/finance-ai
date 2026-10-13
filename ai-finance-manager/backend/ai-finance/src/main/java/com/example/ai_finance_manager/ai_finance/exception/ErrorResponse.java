package com.example.ai_finance_manager.ai_finance.exception;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String message,
        Map<String, String> fieldErrors
) {

    public static ErrorResponse of(int status, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(
                OffsetDateTime.now(ZoneId.of("Asia/Kolkata")),
                status,
                message,
                fieldErrors == null ? Map.of() : fieldErrors);
    }
}
