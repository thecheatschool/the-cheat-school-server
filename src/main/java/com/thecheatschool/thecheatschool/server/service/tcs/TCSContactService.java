package com.thecheatschool.thecheatschool.server.service.tcs;

import com.thecheatschool.thecheatschool.server.model.tcs.TCSContact;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSContactRequest;
import com.thecheatschool.thecheatschool.server.repository.TCSContactRepository;
import com.thecheatschool.thecheatschool.server.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TCSContactService {

    private final TCSEmailService emailService;
    private final TCSContactRepository contactRepository;

    public void processContactForm(TCSContactRequest request) {
        String requestId = generateRequestId();

        log.info("[{}] Processing contact form submission from college: {}, branch: {}",
                requestId, request.getCollege(), request.getBranch());

        try {
            // 1. Send email to YOU (with user's details)
            log.debug("[{}] Attempting to send contact notification email", requestId);
            emailService.sendContactEmail(request);

            // 2. Send confirmation email to USER
            // emailService.sendConfirmationEmailToUser(request);

            log.info("[{}] Contact form processed successfully - email delivery successful", requestId);

            // Both emails sent successfully - DON'T save to database

        } catch (Exception e) {
            // Email failed - Save to database as backup
            log.warn("[{}] Email delivery failed, initiating database fallback backup", requestId);
            saveFailedSubmission(request, requestId);
            // Do not rethrow; we consider this a soft failure and return success to the user
            // while preserving the data for follow-up.
            log.info("[{}] Email failure handled gracefully - user data saved to database for follow-up", requestId);
        }
    }

    private void saveFailedSubmission(TCSContactRequest request, String requestId) {
        TCSContact submission = new TCSContact();
        submission.setFullName(InputSanitizer.sanitize(request.getFullName()));
        submission.setEmail(request.getEmail()); // Email is already validated in request
        submission.setPhoneNumber(InputSanitizer.sanitize(request.getPhoneNumber()));
        submission.setCollege(InputSanitizer.sanitize(request.getCollege()));
        submission.setYearOfStudy(InputSanitizer.sanitize(request.getYearOfStudy()));
        submission.setBranch(InputSanitizer.sanitize(request.getBranch()));
        submission.setHearAboutUs(InputSanitizer.sanitize(request.getHearAboutUs()));
        submission.setHearAboutUsOther(InputSanitizer.sanitize(request.getHearAboutUsOther()));
        submission.setStatus("EMAIL_FAILED");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setExpiresAt(LocalDateTime.now().plusDays(30));

        TCSContact saved = contactRepository.save(submission);
        log.info("[{}] Failed submission saved to database with ID: {}, expires: {}",
                requestId, saved.getId(), saved.getExpiresAt());
    }

    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
}