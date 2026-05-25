package com.emiratiyo.api.controller;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import com.emiratiyo.api.dto.ApiResponse;
import com.emiratiyo.api.dto.EmiraAnalysisRequest;
import com.emiratiyo.api.service.EmiraService;
import com.emiratiyo.api.util.EmiraAuthUtil;

@Slf4j
@RestController
@RequestMapping("/api/v1/internal/emira")
@RequiredArgsConstructor
public class EmiraController {

    private final EmiraService emiraService;
    
    @Qualifier("emiraBucket")
    private final Bucket emiraBucket;

    @Value("${emira.internal.secret:}")
    private String internalSecret;

    @PostMapping(value = "/analyse")
    public Mono<ResponseEntity<ApiResponse<String>>> analyse(
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey,
            @RequestBody EmiraAnalysisRequest request) {

        if (!EmiraAuthUtil.isAuthorized(internalKey, internalSecret)) {
            return Mono.just(ResponseEntity.status(403).body(ApiResponse.error("Unauthorized")));
        }

        if (!emiraBucket.tryConsume(1)) {
            return Mono.just(ResponseEntity.status(429).body(ApiResponse.error("Rate limit exceeded. Please try again later.")));
        }

        return emiraService.analyse(request)
                .map(text -> ResponseEntity.ok(ApiResponse.success(text)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(503).body(ApiResponse.error(e.getMessage()))));
    }
}