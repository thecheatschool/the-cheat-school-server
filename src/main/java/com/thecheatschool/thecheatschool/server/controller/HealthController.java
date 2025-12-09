package com.thecheatschool.thecheatschool.server.controller;

import com.thecheatschool.thecheatschool.server.model.ApiResponse;
import com.thecheatschool.thecheatschool.server.repository.ContactRepository;
import com.thecheatschool.thecheatschool.server.service.SanityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final ContactRepository contactRepository;
    private final SanityService sanityService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> details = new HashMap<>();

        boolean dbHealthy = false;
        boolean cmsHealthy = false;

        // Basic DB health: try a lightweight query
        try {
            long count = contactRepository.count();
            dbHealthy = true;
            details.put("databaseCountCheck", count);
        } catch (Exception e) {
            log.error("Database health check failed", e);
        }

        // Sanity CMS health
        try {
            cmsHealthy = sanityService.isHealthy();
        } catch (Exception e) {
            log.error("Sanity health check threw an exception", e);
            cmsHealthy = false;
        }

        details.put("database", dbHealthy ? "UP" : "DOWN");
        details.put("cms", cmsHealthy ? "UP" : "DOWN");
        details.put("app", "UP");
        details.put("timestamp", Instant.now().toString());

        boolean overallHealthy = dbHealthy && cmsHealthy;

        String message = overallHealthy
                ? "All systems operational"
                : "One or more dependencies are experiencing issues";

        ApiResponse<Map<String, Object>> body =
                new ApiResponse<>(overallHealthy ? "success" : "error", details, message);

        if (overallHealthy) {
            return ResponseEntity.ok(body);
        } else {
            return ResponseEntity.status(503).body(body);
        }
    }
}


