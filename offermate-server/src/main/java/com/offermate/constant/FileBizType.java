package com.offermate.constant;

import java.util.Arrays;
import java.util.Set;

public enum FileBizType {

    AVATAR("avatar", "avatar/", Set.of("jpg", "jpeg", "png", "webp"), 5),
    COMPANY_LOGO("companyLogo", "company-logo/", Set.of("jpg", "jpeg", "png", "webp"), 5),
    RESUME_ATTACHMENT("resumeAttachment", "resume-attachment/", Set.of("pdf", "doc", "docx"), 10),
    CHAT_IMAGE("chatImage", "chat-image/", Set.of("jpg", "jpeg", "png", "webp"), 5);

    private final String type;
    private final String prefix;
    private final Set<String> allowExtensions;
    private final long maxSizeMb;

    FileBizType(String type, String prefix, Set<String> allowExtensions, long maxSizeMb) {
        this.type = type;
        this.prefix = prefix;
        this.allowExtensions = allowExtensions;
        this.maxSizeMb = maxSizeMb;
    }

    public String getPrefix() {
        return prefix;
    }

    public Set<String> getAllowExtensions() {
        return allowExtensions;
    }

    public long getMaxSizeMb() {
        return maxSizeMb;
    }

    public long getMaxSizeBytes() {
        return maxSizeMb * 1024 * 1024;
    }

    public static FileBizType of(String type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .findFirst()
                .orElse(null);
    }
}
