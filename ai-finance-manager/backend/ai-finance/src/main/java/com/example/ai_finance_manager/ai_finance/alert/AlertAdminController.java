package com.example.ai_finance_manager.ai_finance.alert;


import com.example.ai_finance_manager.ai_finance.exception.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/admin")
public class AlertAdminController {

    private final AlertService alertService;
    private final String cronToken;

    public AlertAdminController(AlertService alertService,
                                @Value("${app.cron-token:}") String cronToken) {
        this.alertService = alertService;
        this.cronToken = cronToken;
    }

    @PostMapping("/run-alerts")
    public ResponseEntity<?> runAlerts(
            @RequestHeader(value = "X-Cron-Token", required = false) String provided) {
        if (!isAuthorized(provided)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of(401, "Invalid or missing cron token", null));
        }
        return ResponseEntity.ok(alertService.run());
    }

    private boolean isAuthorized(String provided) {
        if (cronToken == null || cronToken.isBlank() || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                cronToken.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }
}