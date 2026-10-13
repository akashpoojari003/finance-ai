package com.example.ai_finance_manager.ai_finance.insight;

import com.example.ai_finance_manager.ai_finance.dashboard.DashboardResponse;
import com.example.ai_finance_manager.ai_finance.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final DashboardService dashboardService;
    private final InsightService insightService;

    @GetMapping
    public ResponseEntity<InsightsResponse> get(
            @RequestParam(name = "month", required = false) String month) {
        DashboardResponse dashboard = dashboardService.getDashboard(month);
        return ResponseEntity.ok(new InsightsResponse(insightService.monthlyTips(dashboard)));
    }
}
