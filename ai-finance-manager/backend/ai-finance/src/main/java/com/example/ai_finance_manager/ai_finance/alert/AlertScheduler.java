package com.example.ai_finance_manager.ai_finance.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.alerts", name = "scheduler-enabled",
        havingValue = "true", matchIfMissing = true)
public class AlertScheduler {

    private final AlertService alertService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void dailyCheck() {
        log.info("Daily budget alert check started");
        alertService.run();
    }
}