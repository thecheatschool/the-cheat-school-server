package com.thecheatschool.thecheatschool.server.service.tcs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.thecheatschool.thecheatschool.server.model.tcs.TCSContactRequest;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TCSEmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${contact.recipient.email}")
    private String recipientEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendContactEmail(TCSContactRequest request) {
        String url = "https://api.resend.com/emails";
        String emailHash = maskEmail(request.getEmail());

        log.info("Starting email send process for contact from college: {}, branch: {}",
                request.getCollege(), request.getBranch());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + resendApiKey);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("from", "onboarding@resend.dev");
        emailData.put("to", new String[]{recipientEmail});
        emailData.put("reply_to", request.getEmail());
        emailData.put("subject", "New Contact Form: " + request.getFullName());
        emailData.put("html", buildEmailHtml(request));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(emailData, headers);

        try {
            log.debug("Sending HTTP request to Resend API endpoint");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully to recipient, reply-to: {}", emailHash);
            } else {
                log.warn("Failed to send email. HTTP Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to send email");
            }
        } catch (Exception e) {
            log.error("Error sending email via Resend API for contact email: {}", emailHash, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailHtml(TCSContactRequest request) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<link href='https://fonts.googleapis.com/css2?family=Antonio:wght@100..700&family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&family=Oswald:wght@200..700&display=swap' rel='stylesheet'>");
        html.append("</head>");
        html.append("<body style='margin: 0; padding: 20px; background-color: #f8f9fa; font-family: \"Inter\", Arial, sans-serif;'>");

        html.append("<div style='max-width: 100%; width: 100%; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>");

        // Header section
        html.append("<div style='background-color: #2b2b2b; padding: 25px 20px; text-align: center;'>");
        html.append("<h1 style='font-family: \"Antonio\", sans-serif; color: #ffffff; margin: 0; font-size: 22px; font-weight: 600; letter-spacing: 1px; line-height: 1.3;'>REGISTRATION REQUEST</h1>");
        html.append("</div>");

        // Content container
        html.append("<div style='padding: 0;'>");

        // Using div-based layout instead of table for better mobile responsiveness
        html.append("<div style='padding: 0;'>");

        addMobileRow(html, "Full Name", request.getFullName());
        addMobileRow(html, "Email", request.getEmail());
        addMobileRow(html, "Phone Number (WhatsApp)", request.getPhoneNumber());
        addMobileRow(html, "College/University", request.getCollege());
        addMobileRow(html, "Year of Study", request.getYearOfStudy());
        addMobileRow(html, "Branch", request.getBranch());
        addMobileRow(html, "How They Heard About Us", request.getHearAboutUs());

        if (request.getHearAboutUsOther() != null && !request.getHearAboutUsOther().isEmpty()) {
            addMobileRow(html, "Other Details", request.getHearAboutUsOther());
        }

        html.append("</div>");

        // Database link section
        html.append("<div style='margin-top: 10px; padding: 25px 20px; background-color: #f5f5f5; text-align: center; border-top: 3px solid #e83f25;'>");
        html.append("<p style='font-family: \"Oswald\", sans-serif; color: #2b2b2b; margin: 0 0 12px 0; font-size: 14px; letter-spacing: 0.5px; line-height: 1.4;'>VIEW DATABASE IN CASE OF EMAIL SERVICE FAILURES</p>");
        html.append("<a href='https://console.neon.tech/app/projects/royal-credit-24080024/branches/br-wild-thunder-ahrdgz4k/tables' style='font-family: \"Inter\", sans-serif; color: #e83f25; font-size: 14px; text-decoration: none; font-weight: 500; word-break: break-all; display: inline-block; padding: 8px 16px; background-color: #2b2b2b; border-radius: 5px; margin-top: 8px;'>");
        html.append("View Database Console");
        html.append("</a>");
        html.append("</div>");

        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private void addMobileRow(StringBuilder html, String label, String value) {
        if (value != null && !value.isEmpty()) {
            html.append("<div style='padding: 18px 20px; border-bottom: 1px solid #eeeeee;'>");
            html.append("<div style='margin-bottom: 6px;'>");
            html.append("<strong style='font-family: \"Oswald\", sans-serif; color: #2b2b2b; font-size: 13px; letter-spacing: 0.5px; text-transform: uppercase; display: block;'>").append(label).append("</strong>");
            html.append("</div>");
            html.append("<div style='font-family: \"Inter\", sans-serif; color: #2b2b2b; font-size: 15px; line-height: 1.5; word-wrap: break-word;'>").append(value).append("</div>");
            html.append("</div>");
        }
    }

    // Send confirmation email to the USER
    public void sendConfirmationEmailToUser(TCSContactRequest request) {
        String url = "https://api.resend.com/emails";
        String emailHash = maskEmail(request.getEmail());

        log.info("Sending confirmation email to user: {}", emailHash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + resendApiKey);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("from", "onboarding@resend.dev");
        emailData.put("to", new String[]{request.getEmail()}); // Send to USER's email
        emailData.put("subject", "Thank You for Contacting The Cheat School!");
        // emailData.put("html", buildUserConfirmationHtml(request));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(emailData, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Confirmation email sent to user successfully");
            } else {
                log.warn("Failed to send confirmation email to user. HTTP Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            // Don't throw exception - we don't want to fail the main process if confirmation fails
            log.warn("Error sending confirmation email to user, proceeding with main process", e);
        }
    }

    // Build the HTML for user confirmation email
//    private String buildUserConfirmationHtml(ContactRequest request) {
//        return String.format("""
//        <div style="font-family: Arial, sans-serif; padding: 30px; max-width: 600px; margin: 0 auto; background-color: #f9f9f9;">
//            <div style="background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
//                <h1 style="color: #4CAF50; margin-bottom: 20px;">Thank You, %s! 🎉</h1>
//
//                <p style="font-size: 16px; color: #333; line-height: 1.6;">
//                    Thanks for reaching out to <strong>The Cheat School</strong>!
//                </p>
//
//                <p style="font-size: 16px; color: #333; line-height: 1.6;">
//                    We've received your registration and our team will get back to you soon.
//                </p>
//
//                <div style="background-color: #f0f8ff; padding: 20px; border-left: 4px solid #4CAF50; margin: 30px 0;">
//                    <p style="margin: 0; font-size: 16px; color: #333;">
//                        <strong>Meanwhile, join our WhatsApp community for:</strong>
//                    </p>
//                    <ul style="margin-top: 10px; color: #555;">
//                        <li>Latest updates and resources</li>
//                        <li>Connect with other students</li>
//                        <li>Exclusive study materials</li>
//                    </ul>
//                    <a href="https://chat.whatsapp.com/YOUR_WHATSAPP_LINK"
//                       style="display: inline-block; margin-top: 15px; padding: 12px 30px; background-color: #25D366; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
//                        Join WhatsApp Community
//                    </a>
//                </div>
//
//                <div style="text-align: center; margin: 30px 0;">
//                            <a href="https://www.linkedin.com/company/the-cheat-school/?originalSubdomain=in"\s
//                               style="display: inline-block; margin: 10px; padding: 12px 25px; background-color: #0088cc; color: white; text-decoration: none; border-radius: 5px;">
//                                LinkedIn
//                            </a>
//                            <a href="https://www.instagram.com/thecheatschool"\s
//                               style="display: inline-block; margin: 10px; padding: 12px 25px; background-color: #E4405F; color: white; text-decoration: none; border-radius: 5px;">
//                                Instagram
//                            </a>
//                        </div>
//
//                <p style="font-size: 14px; color: #666; margin-top: 30px;">
//                    If you have any urgent queries, feel free to reply to this email.
//                </p>
//
//                <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
//
//                <p style="font-size: 12px; color: #999; text-align: center;">
//                    The Cheat School Team<br>
//                    <a href="https://thecheatschool.com" style="color: #4CAF50;">www.thecheatschool.com</a>
//                </p>
//            </div>
//        </div>
//        """, request.getFullName());
//    }

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

        String masked = localPart.charAt(0) + "*".repeat(localPart.length() - 2) + localPart.charAt(localPart.length() - 1) + "@" + domain;
        return masked;
    }
}