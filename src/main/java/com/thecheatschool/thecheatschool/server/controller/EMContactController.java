package com.thecheatschool.thecheatschool.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecheatschool.thecheatschool.server.model.ApiResponse;
import com.thecheatschool.thecheatschool.server.model.em.EMContact;
import com.thecheatschool.thecheatschool.server.model.em.EMContactRequest;
import com.thecheatschool.thecheatschool.server.repository.EMContactRepository;
import com.thecheatschool.thecheatschool.server.service.em.EMContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/em/contact")
@CrossOrigin(origins = {"http://localhost:5173", "https://*.vercel.app"})
@RequiredArgsConstructor
@Slf4j
@Validated
public class EMContactController {

    private final EMContactService contactService;
    private final EMContactRepository contactRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<String>> contactInfo() {
        return ResponseEntity.ok(new ApiResponse<>("success", "Use POST with JSON body to submit the Emiratiyo Investments contact form."));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> submitContactForm(@Valid @RequestBody EMContactRequest request) {
        log.info("Received EM contact form submission from: {}", request.getEmail());

        try {
            contactService.processContactForm(request);
            return ResponseEntity.ok(new ApiResponse<>("success", "Message sent successfully! We'll get back to you soon."));
        } catch (Exception e) {
            log.error("Error processing EM contact form", e);
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Failed to send message. Please try again."));
        }
    }

    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<EMContact>>> getFailedSubmissions() {
        log.info("Fetching failed EM contact submissions");
        List<EMContact> failed = contactRepository.findByStatus("EMAIL_FAILED");
        return ResponseEntity.ok(new ApiResponse<>("success", failed));
    }

    @PostMapping(consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<ApiResponse<String>> submitContactFormFallback(@RequestBody String body) {
        try {
            EMContactRequest request = objectMapper.readValue(body, EMContactRequest.class);
            return submitContactForm(request);
        } catch (Exception ex) {
            log.error("Failed to parse EM contact request body", ex);
            return ResponseEntity.status(400).body(new ApiResponse<>("error", "Invalid request body. Please send JSON with application/json Content-Type."));
        }
    }
}
