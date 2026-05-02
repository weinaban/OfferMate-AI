package com.offermate.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class IpUtils {

    private static final String UNKNOWN = "unknown";

    private IpUtils() {
    }

    public static String getIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = getHeaderIp(request, "X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            int index = ip.indexOf(',');
            return index > 0 ? ip.substring(0, index).trim() : ip.trim();
        }
        ip = getHeaderIp(request, "X-Real-IP");
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        ip = getHeaderIp(request, "Proxy-Client-IP");
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        ip = getHeaderIp(request, "WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private static String getHeaderIp(HttpServletRequest request, String header) {
        String value = request.getHeader(header);
        if (!StringUtils.hasText(value) || UNKNOWN.equalsIgnoreCase(value)) {
            return null;
        }
        return value.trim();
    }
}
