package com.terstredisproject1.infrastructure.db.redis;

import com.terstredisproject1.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class LoginUserRepository {
    private static final String SESSION_USER_KEY = "session:user:";
    private static final String ONLINE = "online";
    private final StringRedisTemplate stringRedisTemplate;

    public void save(User user) {
        stringRedisTemplate.opsForSet().add(
                SESSION_USER_KEY + user.getId(),
                ONLINE,
                Duration.ofMinutes(30)
        ) ;
    }
}
