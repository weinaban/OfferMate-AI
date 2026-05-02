package com.offermate.service.impl;

import com.offermate.constant.RedisConstants;
import com.offermate.exception.BusinessException;
import com.offermate.service.AiLimitService;
import com.offermate.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiLimitServiceImpl implements AiLimitService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisCacheService redisCacheService;

    @Override
    public void checkAndIncrement(Long userId) {
        String key = RedisConstants.AI_LIMIT_KEY + userId + ":" + LocalDate.now().format(DATE_FORMATTER);
        Long count = redisCacheService.increment(key);
        if (count == null) {
            log.warn("AI限流Redis不可用，放行 userId={}", userId);
            return;
        }
        if (count == 1L) {
            redisCacheService.expire(key, getTodayRemainDuration());
        }
        if (count > RedisConstants.AI_LIMIT_DAILY_COUNT) {
            throw new BusinessException("今日 AI 调用次数已用完，请明天再试");
        }
    }

    private Duration getTodayRemainDuration() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return Duration.between(now, end);
    }
}
