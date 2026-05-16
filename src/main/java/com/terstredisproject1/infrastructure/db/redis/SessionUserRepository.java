package com.terstredisproject1.infrastructure.db.redis;

import com.terstredisproject1.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class SessionUserRepository {
    @Value("${user.session.ttl.min:3}")
    private int sessionTTL;

    private static final String SESSION_USER_KEY = "session:user:";
    private static final String ONLINE = "online";
    private final StringRedisTemplate stringRedisTemplate;

    public void save(User user) {
        stringRedisTemplate.opsForValue().set(
                getKey(user.getId()),
                ONLINE,
                Duration.ofMinutes(sessionTTL)
        );
    }

    public void delete(User user) {
        stringRedisTemplate.delete(getKey(user.getId()));
    }

    public boolean isUserOnline(long userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(getKey(userId)));
    }

    public boolean refreshSession(long userId) {
         return Boolean.TRUE.equals(stringRedisTemplate.expire(getKey(userId), Duration.ofMinutes(sessionTTL)));
    }

    private static @NonNull String getKey(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be negative or zero");
        }
        return SESSION_USER_KEY + userId;
    }
}
