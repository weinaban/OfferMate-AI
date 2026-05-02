package com.offermate.service.impl;

import com.offermate.constant.RedisConstants;
import com.offermate.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheServiceImpl implements RedisCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean isAvailable() {
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            log.warn("Redis不可用，降级走数据库或本地逻辑");
            return false;
        }
    }

    @Override
    public void set(String key, String value, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Redis写入失败 key={}", safeKey(key), e);
        }
    }

    @Override
    public String get(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis读取失败 key={}", safeKey(key), e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis删除失败 key={}", safeKey(key), e);
        }
    }

    @Override
    public void deleteByPrefix(String prefix) {
        try {
            List<String> keys = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(500).build();
            try (Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("Redis前缀缓存已清理 prefix={}, count={}", prefix, keys.size());
            }
        } catch (Exception e) {
            log.warn("Redis按前缀删除失败 prefix={}", prefix, e);
        }
    }

    @Override
    public Boolean setIfAbsent(String key, String value, Duration ttl) {
        try {
            return stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        } catch (Exception e) {
            log.warn("Redis加锁失败 key={}", safeKey(key), e);
            return null;
        }
    }

    @Override
    public Long increment(String key) {
        try {
            return stringRedisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.warn("Redis计数失败 key={}", safeKey(key), e);
            return null;
        }
    }

    @Override
    public void expire(String key, Duration ttl) {
        try {
            stringRedisTemplate.expire(key, ttl);
        } catch (Exception e) {
            log.warn("Redis设置过期时间失败 key={}", safeKey(key), e);
        }
    }

    @Override
    public Boolean hasKey(String key) {
        try {
            return stringRedisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Redis判断key失败 key={}", safeKey(key), e);
            return null;
        }
    }

    private String safeKey(String key) {
        if (key != null && key.startsWith(RedisConstants.TOKEN_BLACKLIST_KEY)) {
            String token = key.substring(RedisConstants.TOKEN_BLACKLIST_KEY.length());
            return RedisConstants.TOKEN_BLACKLIST_KEY + (token.length() <= 10 ? token : token.substring(0, 10) + "***");
        }
        return key;
    }
}
