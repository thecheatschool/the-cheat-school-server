package com.thecheatschool.thecheatschool.server.service.em;

import com.thecheatschool.thecheatschool.server.model.em.EMBusinessSetupRequest;
import com.thecheatschool.thecheatschool.server.model.em.EMBusinessSetupSubmission;
import com.thecheatschool.thecheatschool.server.repository.EMBusinessSetupRepository;
import com.thecheatschool.thecheatschool.server.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EMBusinessSetupService {

    private final EMEmailService emailService;
    private final EMBusinessSetupRepository businessSetupRepository;

    public void processBusinessSetup(EMBusinessSetupRequest request) {
        String requestId = generateRequestId();
        log.info("[{}] Processing EM-business setup submission", requestId);

        EMBusinessSetupSubmission submission = buildSubmission(request);
        submission.setStatus("PENDING_EMAIL");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setExpiresAt(LocalDateTime.now().plusDays(30));
        submission = businessSetupRepository.save(submission);

        try {
            emailService.sendBusinessSetupEmail(request);
            submission.setStatus("SENT");
            businessSetupRepository.save(submission);
            log.info("[{}] EM business setup processed successfully - email delivery successful", requestId);
        } catch (Exception e) {
            log.warn("[{}] EM business setup email delivery failed, marking submission as EMAIL_FAILED", requestId);
            submission.setStatus("EMAIL_FAILED");
            businessSetupRepository.save(submission);
            log.info("[{}] EM business setup email failure handled gracefully - user data saved to database for follow-up", requestId);
        }
    }

    private EMBusinessSetupSubmission buildSubmission(EMBusinessSetupRequest request) {
        EMBusinessSetupSubmission submission = new EMBusinessSetupSubmission();
        submission.setFullName(InputSanitizer.sanitize(request.getFullName()));
        submission.setEmail(request.getEmail());
        submission.setMobileNumber(InputSanitizer.sanitize(request.getMobileNumber()));
        submission.setCountryOfResidence(InputSanitizer.sanitize(request.getCountryOfResidence()));
        return submission;
    }

    private String generateRequestId() {
        return "EM-BIZ-REQ-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
    }
}
