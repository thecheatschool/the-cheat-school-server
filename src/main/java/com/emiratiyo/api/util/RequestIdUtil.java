package com.emiratiyo.api.util;

import java.util.UUID;

public final class RequestIdUtil {

    private RequestIdUtil() {}

    public static String generate(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
