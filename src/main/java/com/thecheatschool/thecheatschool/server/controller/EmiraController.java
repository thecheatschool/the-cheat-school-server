package com.thecheatschool.thecheatschool.server.controller;

import com.thecheatschool.thecheatschool.server.model.em.EmiraAnalysisRequest;
import com.thecheatschool.thecheatschool.server.service.em.EmiraService;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/internal")
public class EmiraController {

    private final EmiraService emiraService;
    private final Bucket emiraBucket;

    @Value("${emira.internal.secret}")
    private String internalSecret;

    public EmiraController(EmiraService emiraService, @Qualifier("emiraBucket") Bucket emiraBucket) {
        this.emiraService = emiraService;
        this.emiraBucket = emiraBucket;
    }

    @PostMapping(value = "/analyse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyse(
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey,
            @RequestBody EmiraAnalysisRequest request) {

        // 3 minute timeout for long Gemini responses
        SseEmitter emitter = new SseEmitter(180_000L);

        // Validate secret
        if (internalKey == null || !internalKey.equals(internalSecret)) {
            log.warn("Unauthorized access attempt to Emira Analyst");
            try {
                emitter.send(SseEmitter.event().name("error").data("Unauthorised"));
                emitter.complete();
            } catch (Exception e) {
                log.error("Failed to send error event", e);
            }
            return emitter;
        }

        // Rate limiting check
        if (!emiraBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for Emira Analyst");
            try {
                emitter.send(SseEmitter.event().name("error").data("Too many requests. Please wait a moment."));
                emitter.complete();
            } catch (Exception e) {
                log.error("Failed to send error event", e);
            }
            return emitter;
        }

        // Run the analysis asynchronously
        try {
            emiraService.analyse(request, emitter);
        } catch (Exception e) {
            log.error("Failed to start Emira analysis", e);
            try {
                emitter.send(SseEmitter.event().name("error").data("Emira is temporarily unavailable. Please try again."));
                emitter.complete();
            } catch (Exception ex) {
                log.error("Failed to send error event", ex);
            }
        }

        return emitter;
    }
}
