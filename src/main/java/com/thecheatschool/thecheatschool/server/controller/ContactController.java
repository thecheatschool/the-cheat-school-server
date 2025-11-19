package com.thecheatschool.thecheatschool.server.controller;

import com.thecheatschool.thecheatschool.server.model.ApiResponse;
import com.thecheatschool.thecheatschool.server.model.Contact;
import com.thecheatschool.thecheatschool.server.model.ContactRequest;
import com.thecheatschool.thecheatschool.server.repository.ContactRepository;
import com.thecheatschool.thecheatschool.server.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"http://localhost:5173", "https://*.vercel.app"})
@RequiredArgsConstructor
@Slf4j
@Validated
public class ContactController {

    private final ContactService contactService;
    private final ContactRepository contactRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> submitContactForm(
            @Valid @RequestBody ContactRequest request) {

        log.info("Received contact form submission from: {}", request.getEmail());

        try {
            contactService.processContactForm(request);
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Message sent successfully! We'll get back to you soon.")
            );
        } catch (Exception e) {
            log.error("Error processing contact form", e);
            return ResponseEntity.status(500).body(
                    new ApiResponse<>("error", "Failed to send message. Please try again or contact us at thecheatschoolcode@gmail.com")
            );
        }
    }

    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<Contact>>> getFailedSubmissions() {
        log.info("Fetching failed contact submissions");
        List<Contact> failed = contactRepository.findByStatus("EMAIL_FAILED");
        return ResponseEntity.ok(new ApiResponse<>("success", failed));
    }
}