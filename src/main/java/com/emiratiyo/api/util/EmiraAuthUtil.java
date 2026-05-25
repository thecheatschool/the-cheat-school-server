package com.emiratiyo.api.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class EmiraAuthUtil {

    private EmiraAuthUtil() {}
    public static boolean isAuthorized(String key, String secret) {
        if (key == null) return false;
        return MessageDigest.isEqual(
                key.getBytes(StandardCharsets.UTF_8),
                secret.getBytes(StandardCharsets.UTF_8));
    }
}
