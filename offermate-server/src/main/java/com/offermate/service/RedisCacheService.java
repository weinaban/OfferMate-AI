package com.offermate.service;

import java.time.Duration;

public interface RedisCacheService {

    boolean isAvailable();

    void set(String key, String value, Duration ttl);

    String get(String key);

    void delete(String key);

    void deleteByPrefix(String prefix);

    Boolean setIfAbsent(String key, String value, Duration ttl);

    Long increment(String key);

    void expire(String key, Duration ttl);

    Boolean hasKey(String key);
}
