package com.emiratiyo.api.controller;

import com.emiratiyo.api.dto.ApiResponse;
import com.emiratiyo.api.dto.BusinessSetupRequest;
import com.emiratiyo.api.entity.BusinessSetupEntity;
import com.emiratiyo.api.repository.BusinessSetupRepository;
import com.emiratiyo.api.service.BusinessSetupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/business-setup")
@RequiredArgsConstructor
public class BusinessSetupController {

    private final BusinessSetupService businessSetupService;
    private final BusinessSetupRepository businessSetupRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> submit(@Valid @RequestBody BusinessSetupRequest request) {
        log.info("Received business setup submission from: {}", request.email());
        businessSetupService.processBusinessSetup(request);
        return ResponseEntity.ok(ApiResponse.success("Your business setup request has been received. We will contact you shortly."));
    }

    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<BusinessSetupEntity>>> getFailedSubmissions() {
        log.info("Fetching failed business setup submissions");
        List<BusinessSetupEntity> failed = businessSetupRepository.findByStatus("EMAIL_FAILED");
        return ResponseEntity.ok(ApiResponse.success(failed));
    }
}