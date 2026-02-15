package com.thecheatschool.thecheatschool.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecheatschool.thecheatschool.server.model.ApiResponse;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeRequest;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeSignup;
import com.thecheatschool.thecheatschool.server.repository.TCSNotifyMeRepository;
import com.thecheatschool.thecheatschool.server.service.tcs.TCSNotifyMeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notify-me")
@CrossOrigin(origins = {"http://localhost:5173", "https://*.vercel.app"})
@RequiredArgsConstructor
@Slf4j
@Validated
public class TCSNotifyMeController {

    private final TCSNotifyMeService notifyMeService;
    private final TCSNotifyMeRepository notifyMeRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<String>> notifyMeInfo() {
        return ResponseEntity.ok(new ApiResponse<>("success", "Use POST with JSON body to submit notify-me signup."));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> notifyMe(@Valid @RequestBody TCSNotifyMeRequest request) {
        log.info("Received notify-me signup from: {}", request.getEmail());

        try {
            notifyMeService.processNotifyMe(request);
            return ResponseEntity.ok(new ApiResponse<>("success", "Thanks! We'll notify you when new courses launch."));
        } catch (Exception e) {
            log.error("Error processing notify-me signup", e);
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Failed to submit. Please try again."));
        }
    }

    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<TCSNotifyMeSignup>>> getFailedSubmissions() {
        List<TCSNotifyMeSignup> failed = notifyMeRepository.findByStatus("EMAIL_FAILED");
        return ResponseEntity.ok(new ApiResponse<>("success", failed));
    }

    @PostMapping(consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<ApiResponse<String>> notifyMeFallback(@RequestBody String body) {
        try {
            TCSNotifyMeRequest request = objectMapper.readValue(body, TCSNotifyMeRequest.class);
            return notifyMe(request);
        } catch (Exception ex) {
            log.error("Failed to parse notify-me request body", ex);
            return ResponseEntity.status(400).body(new ApiResponse<>("error", "Invalid request body. Please send JSON with application/json Content-Type."));
        }
    }
}
