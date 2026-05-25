package com.emiratiyo.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.emiratiyo.api.dto.BusinessSetupRequest;
import com.emiratiyo.api.entity.BusinessSetupEntity;
import com.emiratiyo.api.repository.BusinessSetupRepository;
import com.emiratiyo.api.util.InputSanitizer;
import com.emiratiyo.api.util.RequestIdUtil;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessSetupService {

    private final EmailService emailService;
    private final BusinessSetupRepository businessSetupRepository;

    public void processBusinessSetup(BusinessSetupRequest request) {
        String requestId = RequestIdUtil.generate("EM-BIZ-REQ-");

        log.info("[{}] Processing EM business setup submission", requestId);

        BusinessSetupEntity submission = BusinessSetupEntity.builder()
                .fullName(InputSanitizer.sanitize(request.fullName()))
                .email(request.email())
                .mobileNumber(InputSanitizer.sanitize(request.mobileNumber()))
                .countryOfResidence(InputSanitizer.sanitize(request.countryOfResidence()))
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        
        businessSetupRepository.save(submission);

        emailService.sendBusinessSetupEmail(request); // async — returns immediately
        log.info("[{}] EM business setup saved, email dispatched asynchronously", requestId);
    }
}