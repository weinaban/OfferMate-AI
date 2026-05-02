package com.offermate.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LogUtils {

    private static final int MAX_LENGTH = 2000;
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldpassword", "newpassword", "token", "authorization",
            "apikey", "secretkey", "accesskey"
    );

    private LogUtils() {
    }

    public static String toSafeJson(ObjectMapper objectMapper, Object value) {
        try {
            JsonNode node = objectMapper.valueToTree(value);
            mask(node);
            return truncate(objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            return truncate(String.valueOf(value));
        }
    }

    public static Object sanitizeArg(Object arg) {
        if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse) {
            return null;
        }
        if (arg instanceof MultipartFile file) {
            return Map.of(
                    "filename", file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                    "size", file.getSize(),
                    "contentType", file.getContentType() == null ? "" : file.getContentType()
            );
        }
        return arg;
    }

    public static String truncate(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= MAX_LENGTH ? value : value.substring(0, MAX_LENGTH) + "...";
    }

    private static void mask(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (isSensitive(field.getKey())) {
                    objectNode.put(field.getKey(), "***");
                } else {
                    mask(field.getValue());
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode item : arrayNode) {
                mask(item);
            }
        }
    }

    private static boolean isSensitive(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        return SENSITIVE_KEYS.contains(key.replace("-", "").replace("_", "").toLowerCase());
    }
}
