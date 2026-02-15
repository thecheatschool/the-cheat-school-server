package com.thecheatschool.thecheatschool.server.service.tcs;

import com.thecheatschool.thecheatschool.server.model.queue.ContactEmailJob;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeRequest;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeSignup;
import com.thecheatschool.thecheatschool.server.repository.TCSNotifyMeRepository;
import com.thecheatschool.thecheatschool.server.service.queue.ContactEmailPublisher;
import com.thecheatschool.thecheatschool.server.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TCSNotifyMeService {

    private final TCSNotifyMeRepository notifyMeRepository;
    private final TCSEmailService emailService;
    private final ContactEmailPublisher contactEmailPublisher;

    @Value("${queue.enabled:false}")
    private boolean queueEnabled;

    public void processNotifyMe(TCSNotifyMeRequest request) {
        String requestId = generateRequestId();

        log.info("[{}] Processing notify-me signup, email: {}", requestId, request.getEmail());

        TCSNotifyMeSignup signup = notifyMeRepository.findByEmail(request.getEmail())
                .orElseGet(TCSNotifyMeSignup::new);

        boolean isNew = (signup.getId() == null);

        signup.setName(InputSanitizer.sanitize(request.getName()));
        signup.setEmail(request.getEmail());
        signup.setPhoneNumber(InputSanitizer.sanitize(request.getPhoneNumber()));

        if (isNew) {
            signup.setSubmittedAt(LocalDateTime.now());
        }
        signup.setUpdatedAt(LocalDateTime.now());
        signup.setStatus("PENDING_EMAIL");
        signup = notifyMeRepository.save(signup);

        if (queueEnabled) {
            log.info("[{}] Queue enabled - publishing notify-me email job for submissionId: {}", requestId, signup.getId());
            ContactEmailJob job = new ContactEmailJob(ContactEmailJob.Type.TCS_NOTIFY_ME, signup.getId(), requestId, Instant.now());
            contactEmailPublisher.publishTcs(job);
            return;
        }

        try {
            emailService.sendNotifyMeEmail(request);
            signup.setStatus("SENT");
            notifyMeRepository.save(signup);
            log.info("[{}] Notify-me processed successfully - email delivery successful", requestId);
        } catch (Exception e) {
            log.warn("[{}] Notify-me email delivery failed, marking as EMAIL_FAILED", requestId);
            signup.setStatus("EMAIL_FAILED");
            notifyMeRepository.save(signup);
        }
    }

    private String generateRequestId() {
        return "NOTIFY-REQ-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
    }
}
