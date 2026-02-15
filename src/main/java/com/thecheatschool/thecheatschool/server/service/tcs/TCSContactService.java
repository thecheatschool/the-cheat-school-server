package com.thecheatschool.thecheatschool.server.service.tcs;

import com.thecheatschool.thecheatschool.server.model.tcs.TCSContact;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSContactRequest;
import com.thecheatschool.thecheatschool.server.repository.TCSContactRepository;
import com.thecheatschool.thecheatschool.server.model.queue.ContactEmailJob;
import com.thecheatschool.thecheatschool.server.service.queue.ContactEmailPublisher;
import com.thecheatschool.thecheatschool.server.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TCSContactService {

    private final TCSEmailService emailService;
    private final TCSContactRepository contactRepository;

    private final ContactEmailPublisher contactEmailPublisher;

    @Value("${queue.enabled:false}")
    private boolean queueEnabled;

    public void processContactForm(TCSContactRequest request) {
        String requestId = generateRequestId();

        log.info("[{}] Processing contact form submission from college: {}, branch: {}",
                requestId, request.getCollege(), request.getBranch());

        // Persist first so no data is lost even if queue/email is down
        TCSContact submission = buildSubmission(request);
        submission.setStatus("PENDING_EMAIL");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setExpiresAt(LocalDateTime.now().plusDays(30));
        submission = contactRepository.save(submission);

        if (queueEnabled) {
            log.info("[{}] Queue enabled - publishing email job for submissionId: {}", requestId, submission.getId());
            ContactEmailJob job = new ContactEmailJob(ContactEmailJob.Type.TCS_CONTACT, submission.getId(), requestId, Instant.now());
            contactEmailPublisher.publishTcs(job);
            return;
        }

        // Fallback to current behavior when queue is disabled
        try {
            log.debug("[{}] Queue disabled - attempting to send contact notification email", requestId);
            emailService.sendContactEmail(request);
            submission.setStatus("SENT");
            contactRepository.save(submission);
            log.info("[{}] Contact form processed successfully - email delivery successful", requestId);
        } catch (Exception e) {
            log.warn("[{}] Email delivery failed, marking submission as EMAIL_FAILED", requestId);
            submission.setStatus("EMAIL_FAILED");
            contactRepository.save(submission);
            log.info("[{}] Email failure handled gracefully - user data saved to database for follow-up", requestId);
        }
    }

    private TCSContact buildSubmission(TCSContactRequest request) {
        TCSContact submission = new TCSContact();
        submission.setFullName(InputSanitizer.sanitize(request.getFullName()));
        submission.setEmail(request.getEmail()); // Email is already validated in request
        submission.setPhoneNumber(InputSanitizer.sanitize(request.getPhoneNumber()));
        submission.setCollege(InputSanitizer.sanitize(request.getCollege()));
        submission.setYearOfStudy(InputSanitizer.sanitize(request.getYearOfStudy()));
        submission.setBranch(InputSanitizer.sanitize(request.getBranch()));
        submission.setHearAboutUs(InputSanitizer.sanitize(request.getHearAboutUs()));
        submission.setHearAboutUsOther(InputSanitizer.sanitize(request.getHearAboutUsOther()));
        return submission;
    }

    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
}