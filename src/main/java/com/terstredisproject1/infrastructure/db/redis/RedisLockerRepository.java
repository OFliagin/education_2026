package com.terstredisproject1.infrastructure.db.redis;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisLockerRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private final String REDIS_KEY_PREFIX = "locker:";

    @Value("${redis.lock.timeout.seconds:30}")
    private long lockTimeoutSeconds;

    public boolean lock(String lockerId, UUID lockUuid) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(getKey(lockerId, lockUuid), "locked", Duration.ofSeconds(lockTimeoutSeconds)));
    }

    public void unlock(String lockerId, UUID lockUuid) {
        stringRedisTemplate.delete(getKey(lockerId, lockUuid));
    }

    private @NonNull String getKey(String lockerId, UUID lockUuid) {
        return REDIS_KEY_PREFIX + lockerId+":"+lockUuid;
    }
}
