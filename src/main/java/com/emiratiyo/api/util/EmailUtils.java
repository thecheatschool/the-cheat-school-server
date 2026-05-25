package com.emiratiyo.api.util;

public final class EmailUtils {

    private EmailUtils() {}
    
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "unknown";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "*@" + domain;
        }

        return localPart.charAt(0)
                + "*".repeat(localPart.length() - 2)
                + localPart.charAt(localPart.length() - 1)
                + "@" + domain;
    }
}
