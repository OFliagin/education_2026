package com.terstredisproject1.infrastructure.db.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "ai:token-usage:";


    public void incrementTokenUsage(long tokenUsage, long userId) {
        final String key = getKey(userId);
        final Long currentUsage = stringRedisTemplate.opsForValue().increment(key, tokenUsage);
        if (currentUsage != null && currentUsage == tokenUsage) {
            stringRedisTemplate.expire(key, ttlUntilEndOfUtcDay());
        }
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
