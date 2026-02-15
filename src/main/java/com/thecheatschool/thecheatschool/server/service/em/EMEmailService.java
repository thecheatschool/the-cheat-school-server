package com.thecheatschool.thecheatschool.server.service.em;

import com.thecheatschool.thecheatschool.server.model.em.EMContactRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EMEmailService {

    @Value("${em.resend.api.key}")
    private String resendApiKey;

    @Value("${em.contact.recipient.email}")
    private String recipientEmail;

    @Value("${em.contact.from.email:onboarding@resend.dev}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendContactEmail(EMContactRequest request) {
        String url = "https://api.resend.com/emails";
        String emailHash = maskEmail(request.getEmail());

        log.info("Starting EM email send process, subject: {}, reply-to: {}", request.getSubject(), emailHash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + resendApiKey);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("from", fromEmail);
        emailData.put("to", new String[]{recipientEmail});
        emailData.put("reply_to", request.getEmail());
        emailData.put("subject", "Emiratiyo Investments - " + safeSubject(request.getSubject()) + " (" + request.getName() + ")");
        emailData.put("html", buildEmailHtml(request));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(emailData, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("EM email sent successfully, reply-to: {}", emailHash);
            } else {
                log.warn("EM email failed. HTTP Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to send email");
            }
        } catch (Exception e) {
            log.error("Error sending EM email via Resend API, reply-to: {}", emailHash, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailHtml(EMContactRequest request) {
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
        addRow(html, "Name", request.getName(), black, grey);
        addRow(html, "Company", request.getCompany(), black, grey);
        addRow(html, "Phone", request.getPhone(), black, grey);
        addRow(html, "Email", request.getEmail(), black, grey);
        addRow(html, "Subject", request.getSubject(), black, grey);
        addRow(html, "Message", escapeHtml(request.getMessage()).replace("\n", "<br/>") , black, grey);
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

    private String safeSubject(String subject) {
        if (subject == null) {
            return "Contact";
        }
        String trimmed = subject.trim();
        return trimmed.isEmpty() ? "Contact" : trimmed;
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
        if (email == null || !email.contains("@")) {
            return "unknown";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "*@" + domain;
        }

        return localPart.charAt(0) + "*".repeat(localPart.length() - 2) + localPart.charAt(localPart.length() - 1) + "@" + domain;
    }
}
