package com.asyncsite.notiservice.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for masking personally identifiable information.
 */
public final class MaskingUtil {

    private MaskingUtil() {}

    /**
     * Masks the middle of a personal name.
     * Examples:
     * - "홍길동" -> "홍*동"
     * - "이순신" -> "이*신"
     * - "AB" -> "A*"
     * - "A" -> "*"
     */
    public static String maskNameMiddle(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        String trimmed = name.trim();
        int len = trimmed.length();
        if (len == 1) {
            return "*";
        }
        if (len == 2) {
            return trimmed.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(trimmed.charAt(0));
        for (int i = 1; i < len - 1; i++) {
            sb.append('*');
        }
        sb.append(trimmed.charAt(len - 1));
        return sb.toString();
    }

    /**
     * Returns a shallow copy of variables map with sensitive name fields masked.
     * Currently masks keys: userName, recipientName, proposerName.
     */
    public static Map<String, Object> maskVariablesForDisplay(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return variables;
        }
        Map<String, Object> masked = new HashMap<>(variables);
        maskKey(masked, "userName");
        maskKey(masked, "recipientName");
        maskKey(masked, "proposerName");
        return masked;
    }

    private static void maskKey(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof String s) {
            map.put(key, maskNameMiddle(s));
        }
    }
}
