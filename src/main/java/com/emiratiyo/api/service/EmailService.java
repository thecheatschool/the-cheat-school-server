package com.emiratiyo.api.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.emiratiyo.api.dto.BusinessSetupRequest;
import com.emiratiyo.api.dto.ContactRequest;
import com.emiratiyo.api.exception.EmailDispatchException;
import com.emiratiyo.api.util.EmailUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    @Value("${em.resend.api.key}")
    private String resendApiKey;

    @Value("${em.contact.recipient.email}")
    private String recipientEmail;

    @Value("${em.contact.from.email:onboarding@resend.dev}")
    private String fromEmail;

    private final WebClient webClient;

    @Async("taskExecutor")
    @CircuitBreaker(name = "resendEmail", fallbackMethod = "fallbackEmail")
    public void sendBusinessSetupEmail(BusinessSetupRequest request) {
        String url = "https://api.resend.com/emails";
        String emailHash = maskEmail(request.email());

        log.info("Starting EM business setup email send process, reply-to: {}", emailHash);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("from", fromEmail);
        emailData.put("to", new String[]{recipientEmail});
        emailData.put("reply_to", request.email());
        emailData.put("subject", "Emiratiyo Investments - Business Setup Request (" + request.fullName() + ")");
        emailData.put("html", buildBusinessSetupEmailHtml(request));

        webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emailData)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("EM business setup email sent successfully, reply-to: {}", emailHash))
                .doOnError(e -> log.error("Error sending EM business setup email via Resend API, reply-to: {}", emailHash, e))
                .subscribe();
    }

    @Async("taskExecutor")
    @CircuitBreaker(name = "resendEmail", fallbackMethod = "fallbackEmail")
    public void sendContactEmail(ContactRequest request) {
        String url = "https://api.resend.com/emails";
        String emailHash = maskEmail(request.email());

        log.info("Starting EM email send process, reply-to: {}", emailHash);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("from", fromEmail);
        emailData.put("to", new String[]{recipientEmail});
        emailData.put("reply_to", request.email());
        emailData.put("subject", "Emiratiyo Investments - New Contact Message (" + request.name() + ")");
        emailData.put("html", buildEmailHtml(request));

        webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emailData)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("EM email sent successfully, reply-to: {}", emailHash))
                .doOnError(e -> log.error("Error sending EM email via Resend API, reply-to: {}", emailHash, e))
                .subscribe();
    }

    /**
     * Fallback for Resend Email Circuit Breaker
     */
    private void fallbackEmail(Object request, Throwable t) {
        log.error("Circuit breaker opened for Resend Email Service. Failing gracefully. Error: {}", t.getMessage());
        // In a real app, we might save to a 'failed_emails' table here for retry later
    }

    private String buildEmailHtml(ContactRequest request) {
        final String primary = "#e83f25";
        final String black = "#000000";
        final String grey = "#939393";
        final String white = "#f7f7f7";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@100..900&display=swap' rel='stylesheet'>");
        html.append("</head>");
        html.append("<body style='margin:0; padding:20px; background-color:").append(white).append("; font-family: Inter, Arial, sans-serif;'>");

        html.append("<div style='max-width:720px; margin:0 auto; background-color:#ffffff; border:1px solid #eeeeee; border-radius:12px; overflow:hidden;'>");

        // Header
        html.append("<div style='background-color:").append(black).append("; padding:22px 20px;'>");
        html.append("<div style='color:#ffffff; font-size:16px; letter-spacing:0.3px; font-weight:700;'>NEW CONTACT MESSAGE</div>");
        html.append("<div style='color:").append(grey).append("; font-size:12px; margin-top:6px;'>Emiratiyo Investments Website</div>");
        html.append("</div>");

        // Content
        html.append("<div style='padding:0;'>");
        addRow(html, "Name", request.name(), black, grey);
        addRow(html, "Phone", request.phone(), black, grey);
        addRow(html, "Email", request.email(), black, grey);
        String message = request.message();
        if (message == null || message.trim().isEmpty()) {
            message = "(No message provided)";
        }
        addRow(html, "Message", escapeHtml(message).replace("\n", "<br/>") , black, grey);
        html.append("</div>");

        // Footer
        html.append("<div style='padding:18px 20px; background-color:").append(white).append("; border-top:3px solid ").append(primary).append(";'>");
        html.append("<div style='color:").append(grey).append("; font-size:12px;'>You can reply directly to this email to respond to the sender.</div>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private String buildBusinessSetupEmailHtml(BusinessSetupRequest request) {
        final String primary = "#e83f25";
        final String black = "#000000";
        final String grey = "#939393";
        final String white = "#f7f7f7";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@100..900&display=swap' rel='stylesheet'>");
        html.append("</head>");
        html.append("<body style='margin:0; padding:20px; background-color:").append(white).append("; font-family: Inter, Arial, sans-serif;'>");

        html.append("<div style='max-width:720px; margin:0 auto; background-color:#ffffff; border:1px solid #eeeeee; border-radius:12px; overflow:hidden;'>");

        // Header
        html.append("<div style='background-color:").append(black).append("; padding:22px 20px;'>");
        html.append("<div style='color:#ffffff; font-size:16px; letter-spacing:0.3px; font-weight:700;'>BUSINESS SETUP REQUEST</div>");
        html.append("<div style='color:").append(grey).append("; font-size:12px; margin-top:6px;'>Emiratiyo Investments Website</div>");
        html.append("</div>");

        // Content
        html.append("<div style='padding:0;'>");
        addRow(html, "Full Name", request.fullName(), black, grey);
        addRow(html, "Email", request.email(), black, grey);
        addRow(html, "Mobile Number", request.mobileNumber(), black, grey);
        addRow(html, "Country of Residence", request.countryOfResidence(), black, grey);
        html.append("</div>");

        // Footer
        html.append("<div style='padding:18px 20px; background-color:").append(white).append("; border-top:3px solid ").append(primary).append(";'>");
        html.append("<div style='color:").append(grey).append("; font-size:12px;'>You can reply directly to this email to respond to the sender.</div>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private void addRow(StringBuilder html, String label, String value, String black, String grey) {
        if (value == null) {
            value = "";
        }
        html.append("<div style='padding:16px 20px; border-bottom:1px solid #f0f0f0;'>");
        html.append("<div style='color:").append(grey).append("; font-size:12px; text-transform:uppercase; letter-spacing:0.6px; margin-bottom:6px;'>").append(escapeHtml(label)).append("</div>");
        html.append("<div style='color:").append(black).append("; font-size:14px; line-height:1.5; word-break:break-word;'>").append(escapeHtml(value)).append("</div>");
        html.append("</div>");
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String maskEmail(String email) {
        return EmailUtils.maskEmail(email);
    }
}