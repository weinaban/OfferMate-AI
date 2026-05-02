package com.offermate.constant;

import java.time.Duration;

public class RedisConstants {

    private RedisConstants() {
    }

    public static final String JOB_PAGE_KEY = "job:page:";
    public static final String JOB_DETAIL_KEY = "job:detail:";
    public static final Duration JOB_PAGE_TTL = Duration.ofMinutes(5);
    public static final Duration JOB_DETAIL_TTL = Duration.ofMinutes(10);

    public static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";

    public static final String DELIVERY_LOCK_KEY = "delivery:lock:";
    public static final Duration DELIVERY_LOCK_TTL = Duration.ofSeconds(10);

    public static final String AI_LIMIT_KEY = "ai:limit:";
    public static final long AI_LIMIT_DAILY_COUNT = 20L;

    public static final String CHAT_UNREAD_KEY = "chat:unread:";
    public static final Duration CHAT_UNREAD_TTL = Duration.ofDays(1);
}
