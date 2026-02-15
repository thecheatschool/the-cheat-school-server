package com.thecheatschool.thecheatschool.server.controller;

import com.thecheatschool.thecheatschool.server.model.ApiResponse;
import com.thecheatschool.thecheatschool.server.repository.TCSContactRepository;
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

    private final TCSContactRepository contactRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> details = new HashMap<>();

        boolean dbHealthy = false;

        // Basic DB health: try a lightweight query
        try {
            long count = contactRepository.count();
            dbHealthy = true;
            details.put("databaseCountCheck", count);
        } catch (Exception e) {
            log.error("Database health check failed", e);
        }

        details.put("database", dbHealthy ? "UP" : "DOWN");
        details.put("app", "UP");
        details.put("timestamp", Instant.now().toString());

        String message;
        if (!dbHealthy) {
            message = "Database is not reachable";
        } else {
            message = "All systems operational";
        }

        ApiResponse<Map<String, Object>> body =
                new ApiResponse<>("success", details, message);

        return ResponseEntity.ok(body);
    }
}



