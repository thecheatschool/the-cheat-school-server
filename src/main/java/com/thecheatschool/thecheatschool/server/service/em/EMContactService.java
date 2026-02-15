package com.thecheatschool.thecheatschool.server.service.em;

import com.thecheatschool.thecheatschool.server.model.em.EMContact;
import com.thecheatschool.thecheatschool.server.model.em.EMContactRequest;
import com.thecheatschool.thecheatschool.server.repository.EMContactRepository;
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
public class EMContactService {

    private final EMEmailService emailService;
    private final EMContactRepository contactRepository;

    private final ContactEmailPublisher contactEmailPublisher;

    @Value("${queue.enabled:false}")
    private boolean queueEnabled;

    public void processContactForm(EMContactRequest request) {
        String requestId = generateRequestId();

        log.info("[{}] Processing EM contact form submission, subject: {}", requestId, request.getSubject());

        EMContact submission = buildSubmission(request);
        submission.setStatus("PENDING_EMAIL");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setExpiresAt(LocalDateTime.now().plusDays(30));
        submission = contactRepository.save(submission);

        if (queueEnabled) {
            log.info("[{}] Queue enabled - publishing EM email job for submissionId: {}", requestId, submission.getId());
            ContactEmailJob job = new ContactEmailJob(ContactEmailJob.Type.EM_CONTACT, submission.getId(), requestId, Instant.now());
            contactEmailPublisher.publishEm(job);
            return;
        }

        try {
            emailService.sendContactEmail(request);
            submission.setStatus("SENT");
            contactRepository.save(submission);
            log.info("[{}] EM contact form processed successfully - email delivery successful", requestId);
        } catch (Exception e) {
            log.warn("[{}] EM email delivery failed, marking submission as EMAIL_FAILED", requestId);
            submission.setStatus("EMAIL_FAILED");
            contactRepository.save(submission);
            log.info("[{}] EM email failure handled gracefully - user data saved to database for follow-up", requestId);
        }
    }

    private EMContact buildSubmission(EMContactRequest request) {
        EMContact submission = new EMContact();
        submission.setName(InputSanitizer.sanitize(request.getName()));
        submission.setCompany(InputSanitizer.sanitize(request.getCompany()));
        submission.setPhone(InputSanitizer.sanitize(request.getPhone()));
        submission.setEmail(request.getEmail());
        submission.setSubject(InputSanitizer.sanitize(request.getSubject()));
        submission.setMessage(InputSanitizer.sanitize(request.getMessage()));
        return submission;
    }

    private String generateRequestId() {
        return "EM-REQ-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
    }
}
