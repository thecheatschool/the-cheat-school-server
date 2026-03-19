package com.thecheatschool.thecheatschool.server.controller;

import com.thecheatschool.thecheatschool.server.model.em.EmiraAnalysis;
import com.thecheatschool.thecheatschool.server.repository.EmiraAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/internal/history")
@RequiredArgsConstructor
public class EmiraHistoryController {

    private final EmiraAnalysisRepository emiraAnalysisRepository;

    @Value("${emira.internal.secret}")
    private String internalSecret;

    @GetMapping
    public ResponseEntity<Object> getHistory(
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {
        
        if (!isAuthorized(internalKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorised");
        }

        List<Map<String, Object>> history = emiraAnalysisRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(analysis -> Map.of(
                        "id", analysis.getId(),
                        "area", analysis.getArea(),
                        "analysisType", analysis.getAnalysisType(),
                        "createdAt", analysis.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAnalysisById(
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey,
            @PathVariable Long id) {

        if (!isAuthorized(internalKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorised");
        }

        return emiraAnalysisRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAnalysis(
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey,
            @PathVariable Long id) {

        if (!isAuthorized(internalKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorised");
        }

        if (!emiraAnalysisRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        emiraAnalysisRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private boolean isAuthorized(String key) {
        return key != null && key.equals(internalSecret);
    }
}
