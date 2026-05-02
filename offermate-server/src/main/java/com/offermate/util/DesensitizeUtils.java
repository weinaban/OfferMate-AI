package com.offermate.util;

import org.springframework.util.StringUtils;

public class DesensitizeUtils {

    private DesensitizeUtils() {
    }

    public static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }
        String value = phone.trim();
        if (value.length() < 7) {
            return value.charAt(0) + "****";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        String value = email.trim();
        int index = value.indexOf('@');
        if (index <= 0) {
            return value.charAt(0) + "***";
        }
        return value.charAt(0) + "***" + value.substring(index);
    }

    public static String maskName(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        String value = name.trim();
        if (value.length() == 1) {
            return value + "*";
        }
        if (value.length() == 2) {
            return value.charAt(0) + "*";
        }
        return value.charAt(0) + "*" + value.charAt(value.length() - 1);
    }
}
