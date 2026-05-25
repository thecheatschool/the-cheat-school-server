package com.emiratiyo.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emiratiyo.api.dto.EmiraHistoryResponse;
import com.emiratiyo.api.entity.EmiraAnalysisEntity;
import com.emiratiyo.api.service.EmiraHistoryService;
import com.emiratiyo.api.util.EmiraAuthUtil;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/internal/emira/history")
@RequiredArgsConstructor
public class EmiraHistoryController {

    private final EmiraHistoryService emiraHistoryService;

    @Value("${emira.internal.secret}")
    private String internalSecret;

    @GetMapping
    public ResponseEntity<List<EmiraHistoryResponse>> getHistory(
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {

        if (!EmiraAuthUtil.isAuthorized(internalKey, internalSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(emiraHistoryService.getHistory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmiraAnalysisEntity> getAnalysisById(
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey,
            @PathVariable Long id) {

        if (!EmiraAuthUtil.isAuthorized(internalKey, internalSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(emiraHistoryService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey,
            @PathVariable Long id) {

        if (!EmiraAuthUtil.isAuthorized(internalKey, internalSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!emiraHistoryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        emiraHistoryService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}