package com.emiratiyo.api.controller;

import com.emiratiyo.api.dto.ApiResponse;
import com.emiratiyo.api.dto.ContactRequest;
import com.emiratiyo.api.entity.ContactEntity;
import com.emiratiyo.api.repository.ContactRepository;
import com.emiratiyo.api.service.ContactService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ContactController {

    private final ContactService contactService;
    private final ContactRepository contactRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> submitContactForm(@Valid @RequestBody ContactRequest request) {
        log.info("Received contact form submission from: {}", request.email());
        contactService.processContactForm(request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully! We'll get back to you soon."));
    }

    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<ContactEntity>>> getFailedSubmissions() {
        log.info("Fetching failed contact submissions");
        List<ContactEntity> failed = contactRepository.findByStatus("EMAIL_FAILED");
        return ResponseEntity.ok(ApiResponse.success(failed));
    }
}
