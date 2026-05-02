package com.offermate.util;

import com.offermate.exception.BusinessException;
import org.springframework.util.StringUtils;

public class AiSecurityUtils {

    private static final int MAX_PROMPT_LENGTH = 8000;
    private static final int LOG_PROMPT_LENGTH = 500;

    private AiSecurityUtils() {
    }

    public static void checkPromptLength(String prompt) {
        if (prompt != null && prompt.length() > MAX_PROMPT_LENGTH) {
            throw new BusinessException("输入内容过长，请精简后再试");
        }
    }

    public static String truncateForLog(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        return text.length() <= LOG_PROMPT_LENGTH ? text : text.substring(0, LOG_PROMPT_LENGTH) + "...";
    }
}
