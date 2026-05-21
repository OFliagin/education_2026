package com.terstredisproject1.infrastructure.db.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisTokenRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "ai:token-usage:";


    public void incrementTokenUsage(long tokenUsage, long userId) {
        log.debug("Incrementing token usage for user: {} with usage: {}", userId, tokenUsage);
        final String key = getKey(userId);
        final Long currentUsage = stringRedisTemplate.opsForValue().increment(key, tokenUsage);
        log.debug("Current token usage for user: {} is: {}", userId, currentUsage);
        if (currentUsage != null && currentUsage == tokenUsage) {
            stringRedisTemplate.expire(key, ttlUntilEndOfUtcDay());
        }
    }

    public long getTokenUsage(long userId) {
        final String key = getKey(userId);
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key))
                .map(Long::parseLong)
                .orElse(0L);
    }


    private String getKey(long userId) {
        String today = LocalDate.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        return KEY_PREFIX + userId + ":" + today;
    }

    private Duration ttlUntilEndOfUtcDay() {
        Instant now = Instant.now();

        Instant nextMidnight = LocalDate.now(ZoneOffset.UTC)
                .plusDays(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        return Duration.between(now, nextMidnight);
    }
}
