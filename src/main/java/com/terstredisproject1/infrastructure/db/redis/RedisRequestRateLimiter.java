package com.terstredisproject1.infrastructure.db.redis;

import com.terstredisproject1.domain.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisRequestRateLimiter {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String COUNTER_PREFIX = "rate-limit:ai-message:";
    @Value("${ai.task.sent.limit:10}")
    private int sentLimit;
    @Value("${ai.task.sent.window.seconds:60}")
    private long windowSeconds;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>("""
                    local count = redis.call('INCR', KEYS[1])
                    if count == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end
                    return count
                    """, Long.class);


    public void checkLimit(Long userId) {
        final String key = getKey(userId);

        long now = System.currentTimeMillis();
        long windowStart = now - Duration.ofSeconds(windowSeconds).toMillis();

        // 1. remove old requests
        stringRedisTemplate.opsForZSet().removeRangeByScore(
                key,
                0,
                windowStart

        );

        // 2. count requests inside current window
        Long currentCount = stringRedisTemplate.opsForZSet().zCard(key);
        if (currentCount == null) {
            throw new IllegalStateException("Failed to read sliding window rate limit counter");
        }

        // 3. reject if limit reached
        if (currentCount >= sentLimit) {
            throw new TooManyRequestsException(
                    "User " + userId + " has reached the limit of "
                            + sentLimit + " messages per "
                            + windowSeconds + " seconds"
            );
        }

        // 4. add current request
        String requestId = now + ":" + UUID.randomUUID();

        stringRedisTemplate.opsForZSet().add(
                key,
                requestId,
                now
        );

        // 5. cleanup inactive users
        stringRedisTemplate.expire(
                key,
                Duration.ofSeconds(windowSeconds * 2)
        );
    }

    /*
     * Lua script runs inside Redis as a single atomic operation.
     * */
    public void checkLimitLuaImpl(long userId) {
        final String key = getKey(userId);
        Long count = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(key),
                windowSeconds
        );
        if (count == null) {
            throw new IllegalStateException("Failed to increment rate limit counter");
        }
        if (count > sentLimit) {
            throw new TooManyRequestsException("User " + userId + " has reached the limit of " + sentLimit + " messages per minute");
        }
    }

    @Deprecated
    public void checkLimitSpringBootImpl(long userId) {
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
