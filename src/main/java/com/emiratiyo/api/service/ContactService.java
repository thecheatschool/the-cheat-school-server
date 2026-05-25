package com.emiratiyo.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.emiratiyo.api.dto.ContactRequest;
import com.emiratiyo.api.entity.ContactEntity;
import com.emiratiyo.api.repository.ContactRepository;
import com.emiratiyo.api.util.InputSanitizer;
import com.emiratiyo.api.util.RequestIdUtil;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final EmailService emailService;
    private final ContactRepository contactRepository;

    public void processContactForm(ContactRequest request) {
        String requestId = RequestIdUtil.generate("EM-REQ-");

        log.info("[{}] Processing EM contact form submission", requestId);

        ContactEntity submission = ContactEntity.builder()
                .name(InputSanitizer.sanitize(request.name()))
                .phone(InputSanitizer.sanitize(request.phone()))
                .email(request.email())
                .message(InputSanitizer.sanitize(request.message()))
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        
        contactRepository.save(submission);

        emailService.sendContactEmail(request); // async — returns immediately
        log.info("[{}] EM contact form saved, email dispatched asynchronously", requestId);
    }
}