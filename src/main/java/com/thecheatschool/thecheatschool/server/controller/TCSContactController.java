package com.thecheatschool.thecheatschool.server.controller;

import com.thecheatschool.thecheatschool.server.model.ApiResponse;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSContact;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSContactRequest;
import com.thecheatschool.thecheatschool.server.repository.TCSContactRepository;
import com.thecheatschool.thecheatschool.server.service.tcs.TCSContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"http://localhost:5173", "https://*.vercel.app"})
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Contact Management", description = "Endpoints for managing contact form submissions and registrations")
public class TCSContactController {

    private final TCSContactService contactService;
    private final TCSContactRepository contactRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "Get contact endpoint information", description = "Returns information about how to submit contact forms")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved contact information")
    })
    public ResponseEntity<ApiResponse<String>> contactInfo() {
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Use POST with JSON body to submit the contact form.")
        );
    }

    @PostMapping
    @Operation(summary = "Submit contact/registration form", description = "Submits a contact form with user details. Sends email and saves to database on email failure.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Form submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error processing the form")
    })
    public ResponseEntity<ApiResponse<String>> submitContactForm(
            @Valid @RequestBody TCSContactRequest request) {

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
    @Operation(summary = "Get failed contact submissions", description = "Retrieves all contact form submissions that failed to send via email (for admin use)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved failed submissions")
    })
    public ResponseEntity<ApiResponse<List<TCSContact>>> getFailedSubmissions() {
        log.info("Fetching failed contact submissions");
        List<TCSContact> failed = contactRepository.findByStatus("EMAIL_FAILED");
        return ResponseEntity.ok(new ApiResponse<>("success", failed));
    }

    /**
     * Fallback handler to tolerate mis-labeled content-types (e.g., text/plain) and still parse JSON.
     */
    @PostMapping(consumes = {MediaType.ALL_VALUE})
    @Operation(hidden = true)
    public ResponseEntity<ApiResponse<String>> submitContactFormFallback(@RequestBody String body) {
        try {
            TCSContactRequest request = objectMapper.readValue(body, TCSContactRequest.class);
            return submitContactForm(request);
        } catch (Exception ex) {
            log.error("Failed to parse contact request body", ex);
            return ResponseEntity.status(400).body(
                    new ApiResponse<>("error", "Invalid request body. Please send JSON with application/json Content-Type.")
            );
        }
    }
}