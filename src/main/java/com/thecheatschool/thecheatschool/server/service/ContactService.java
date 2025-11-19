package com.thecheatschool.thecheatschool.server.service;

import com.thecheatschool.thecheatschool.server.model.ContactRequest;
import com.thecheatschool.thecheatschool.server.model.Contact;
import com.thecheatschool.thecheatschool.server.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final EmailService emailService;
    private final ContactRepository contactRepository;

    public void processContactForm(ContactRequest request) {
        try {
            // 1. Send email to YOU (with user's details)
            emailService.sendContactEmail(request);

            // 2. Send confirmation email to USER
            // emailService.sendConfirmationEmailToUser(request);

            log.info("Contact form processed successfully for: {}", request.getEmail());

            // Both emails sent successfully - DON'T save to database

        } catch (Exception e) {
            // Email failed - Save to database as backup
            log.error("Failed to send email for: {}. Saving to database.", request.getEmail(), e);
            saveFailedSubmission(request);
            throw new RuntimeException("Failed to send email, but your information has been saved");
        }
    }

    private void saveFailedSubmission(ContactRequest request) {
        Contact submission = new Contact();
        submission.setFullName(request.getFullName());
        submission.setEmail(request.getEmail());
        submission.setPhoneNumber(request.getPhoneNumber());
        submission.setCollege(request.getCollege());
        submission.setYearOfStudy(request.getYearOfStudy());
        submission.setBranch(request.getBranch());
        submission.setHearAboutUs(request.getHearAboutUs());
        submission.setHearAboutUsOther(request.getHearAboutUsOther());
        submission.setStatus("EMAIL_FAILED");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setExpiresAt(LocalDateTime.now().plusDays(30));

        contactRepository.save(submission);
        log.info("Failed submission saved to database with ID: {}", submission.getId());
    }
}