package com.example.ai_finance_manager.ai_finance.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/** Lets Prometheus scrape /actuator/prometheus with a static bearer token (env PROMETHEUS_TOKEN). */
public class PrometheusScrapeFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String PATH = "/actuator/prometheus";

    private final String scrapeToken;

    public PrometheusScrapeFilter(String scrapeToken) {
        this.scrapeToken = scrapeToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return scrapeToken == null || scrapeToken.isBlank() || !PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String provided = header.substring(BEARER_PREFIX.length()).trim();
            boolean match = MessageDigest.isEqual(
                    scrapeToken.getBytes(StandardCharsets.UTF_8),
                    provided.getBytes(StandardCharsets.UTF_8));
            if (match) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "prometheus", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            }
        }
        filterChain.doFilter(request, response);
    }
}