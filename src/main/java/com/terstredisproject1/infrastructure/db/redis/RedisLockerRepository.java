package com.terstredisproject1.infrastructure.db.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RedisLockerRepository {
    private static final String UNLOCK_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
            """;

    private final StringRedisTemplate stringRedisTemplate;
    private static final String REDIS_KEY_PREFIX = "locker:";

    @Value("${redis.lock.timeout.seconds:30}")
    private long lockTimeoutSeconds;

    public boolean lock(String lockerId, UUID lockUuid) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(getKey(lockerId), lockUuid.toString(), Duration.ofSeconds(lockTimeoutSeconds)));
    }

    public void unlock(String lockerId, UUID lockUuid) {
        final String key = getKey(lockerId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(UNLOCK_SCRIPT);

        script.setResultType(Long.class);
        Long result = stringRedisTemplate.execute(script, List.of(key), lockUuid.toString());
        log.info("Unlock result for key {} and token {} : {}", key, lockUuid, result);
    }

    private @NonNull String getKey(String lockerId) {
        return REDIS_KEY_PREFIX + lockerId;
    }
}
