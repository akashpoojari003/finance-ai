package com.example.ai_finance_manager.ai_finance.insight;

import com.example.ai_finance_manager.ai_finance.ai.GeminiClient;
import com.example.ai_finance_manager.ai_finance.ai.LruCache;
import com.example.ai_finance_manager.ai_finance.dashboard.DashboardResponse;
import com.example.ai_finance_manager.ai_finance.dashboard.DashboardStatus;
import com.example.ai_finance_manager.ai_finance.repository.projection.CategoryTotal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InsightService {

    private static final int TOP_CATEGORIES = 5;
    private static final int MAX_TIPS = 3;
    private static final int MAX_OUTPUT_TOKENS = 90;

    private final GeminiClient geminiClient;
    private final LruCache<String, List<String>> cache = new LruCache<>(500);

    public InsightService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    /** Sends only aggregated numbers and category names, never user data. */
    public List<String> monthlyTips(DashboardResponse dashboard) {
        if (dashboard.totalSpent() == null || dashboard.totalSpent().signum() == 0) {
            return List.of();
        }

        String prompt = buildPrompt(dashboard);
        List<String> cached = cache.get(prompt);
        if (cached != null) {
            return cached;
        }

        if (geminiClient.isConfigured()) {
            try {
                List<String> tips = parse(geminiClient.generate(prompt, MAX_OUTPUT_TOKENS));
                if (!tips.isEmpty()) {
                    cache.put(prompt, tips);
                    return tips;
                }
            } catch (Exception ex) {
                log.warn("Gemini insight call failed, using fallback tips: {}",
                        ex.getClass().getSimpleName());
            }
        }
        return fallback(dashboard);
    }

    private String buildPrompt(DashboardResponse d) {
        StringBuilder sb = new StringBuilder("Money coach. Spent ").append(fmt(d.totalSpent())).append(" INR");
        if (d.limitAmount() != null && d.percentUsed() != null) {
            sb.append(" of ").append(fmt(d.limitAmount())).append(" limit (")
                    .append(d.percentUsed().setScale(0, RoundingMode.HALF_UP)).append("%)");
        }
        sb.append(". Top: ");
        sb.append(d.byCategory().stream()
                .limit(TOP_CATEGORIES)
                .map(c -> c.category() + " " + fmt(c.total()))
                .collect(Collectors.joining(", ")));
        sb.append(". Give ").append(MAX_TIPS)
                .append(" saving tips, max 12 words each, one per line, no numbering.");
        return sb.toString();
    }

    private List<String> parse(String text) {
        return Arrays.stream(text.split("\\R"))
                .map(line -> line.replace("**", "").replaceFirst("^[\\s\\-*•\\d.)]+", "").trim())
                .filter(line -> !line.isBlank())
                .limit(MAX_TIPS)
                .toList();
    }

    private List<String> fallback(DashboardResponse d) {
        String top = d.byCategory().stream().findFirst().map(CategoryTotal::category).orElse(null);
        String first = d.status() == DashboardStatus.OVER
                ? "You are over budget. Pause non-essential spending."
                : top != null
                ? "Trim " + top + " spending first. It is your biggest cost."
                : "Review your biggest expenses first.";
        return List.of(
                first,
                "Set a weekly spending cap and review it every Sunday.",
                "Wait 24 hours before any non-essential purchase.");
    }

    private String fmt(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
