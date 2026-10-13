package com.example.ai_finance_manager.ai_finance.ai;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(@Value("${app.gemini-key:}") String apiKey,
                        @Value("${app.gemini.model}") String model,
                        @Value("${app.gemini.url}") String url) {
        this.apiKey = apiKey;
        this.model = model;

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Returns the trimmed model text; throws on any HTTP, timeout or empty-response problem. */
    public String generate(String prompt, int maxOutputTokens) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "maxOutputTokens", maxOutputTokens,
                        "temperature", 0,
                        "thinkingConfig", Map.of("thinkingBudget", 0)));

        JsonNode response = restClient.post()
                .uri("/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        String text = response == null ? "" : response
                .path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText("");
        if (text.isBlank()) {
            throw new IllegalStateException("Empty Gemini response");
        }
        return text.trim();
    }
}
