package com.terstredisproject1.infrastructure.db.redis;

import com.terstredisproject1.domain.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisRequestRateLimit {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String COUNTER_PREFIX = "rate-limit:ai-message:";

    @Value("${ai.task.sent.limit:10}")
    private int sentLimit;

    public void checkLimit(long userId) {
        final String key = getKey(userId);
        final Long increment = stringRedisTemplate.opsForValue().increment(key);
        if (increment == null) {
            throw new IllegalStateException("Failed to increment rate limit counter");
        }
        if (increment == 1) {
            stringRedisTemplate.expire(key, Duration.ofMinutes(1));
        }
        if (increment > sentLimit) {
            throw new TooManyRequestsException("User " + userId + " has reached the limit of " + sentLimit + " messages per minute");
        }
    }

    private String getKey(long userId) {
        return COUNTER_PREFIX + userId;
    }
}
