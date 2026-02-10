package com.thecheatschool.thecheatschool.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting interceptor to prevent abuse of the contact form endpoint.
 * Limit: 5 requests per minute per IP address
 */
@Slf4j
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MS = 60 * 1000; // 1 minute
    private static final Map<String, RequestTracker> IP_TRACKER = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Only apply rate limiting to contact endpoint
        if (!request.getRequestURI().startsWith("/api/contact")) {
            return true;
        }

        // Only rate limit POST requests (form submissions)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = getClientIp(request);
        RequestTracker tracker = IP_TRACKER.computeIfAbsent(clientIp, k -> new RequestTracker());

        if (tracker.isRateLimited()) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Too many requests. Maximum 5 requests per minute allowed.\"}");
            return false;
        }

        tracker.recordRequest();
        return true;
    }

    /**
     * Extracts client IP from request, considering X-Forwarded-For header for proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, use the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Helper class to track request counts and timestamps
     */
    private static class RequestTracker {
        private long[] timestamps = new long[MAX_REQUESTS];
        private int currentIndex = 0;
        private boolean isFull = false;

        synchronized boolean isRateLimited() {
            long now = System.currentTimeMillis();

            // If we haven't filled the slots yet
            if (!isFull) {
                return false;
            }

            // Check if the oldest request is still within the time window
            return (now - timestamps[currentIndex]) < TIME_WINDOW_MS;
        }

        synchronized void recordRequest() {
            long now = System.currentTimeMillis();
            timestamps[currentIndex] = now;
            currentIndex = (currentIndex + 1) % MAX_REQUESTS;

            if (currentIndex == 0) {
                isFull = true;
            }
        }
    }
}
