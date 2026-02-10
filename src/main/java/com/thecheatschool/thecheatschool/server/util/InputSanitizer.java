package com.thecheatschool.thecheatschool.server.util;

import java.util.regex.Pattern;

public class InputSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)javascript:|on\\w+\\s*=");

    private InputSanitizer() {
        // Utility class, no instantiation
    }

    /**
     * Sanitizes input by removing HTML tags and script content.
     * Trims whitespace and removes potentially malicious content.
     *
     * @param input the input string to sanitize
     * @return sanitized string, or empty string if input is null
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }

        // Remove HTML tags
        String sanitized = removeHtmlTags(input);

        // Remove script patterns
        sanitized = removeScriptPatterns(sanitized);

        // Trim whitespace
        sanitized = sanitized.trim();

        return sanitized;
    }

    /**
     * Removes HTML tags from input
     */
    private static String removeHtmlTags(String input) {
        return HTML_TAG_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Removes script patterns like 'javascript:' and event handlers like 'onclick='
     */
    private static String removeScriptPatterns(String input) {
        return SCRIPT_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }

    /**
     * Validates phone number format (basic validation)
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Allow digits, +, -, and spaces
        return phone.replaceAll("[\\d+\\-\\s]", "").isEmpty() && phone.length() >= 7;
    }

    /**
     * Checks if input contains suspicious patterns
     */
    public static boolean containsSuspiciousPatterns(String input) {
        if (input == null) {
            return false;
        }

        // Check for common injection patterns
        String lowerInput = input.toLowerCase();
        return lowerInput.contains("<script") ||
               lowerInput.contains("javascript:") ||
               lowerInput.contains("onerror=") ||
               lowerInput.contains("onclick=") ||
               lowerInput.contains("onload=") ||
               lowerInput.contains("--") ||  // SQL comment
               lowerInput.contains("/*") ||  // SQL comment
               lowerInput.contains("*/") ||
               lowerInput.contains("union") ||
               lowerInput.contains("select") ||
               lowerInput.contains("drop") ||
               lowerInput.contains("insert") ||
               lowerInput.contains("update") ||
               lowerInput.contains("delete");
    }

    /**
     * Gets email prefix (before @) for logging purposes
     */
    public static String getEmailPrefix(String email) {
        if (email == null || !email.contains("@")) {
            return "unknown";
        }
        return email.split("@")[0] + "@***";
    }
}
