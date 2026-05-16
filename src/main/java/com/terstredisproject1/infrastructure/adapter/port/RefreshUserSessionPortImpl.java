package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.infrastructure.db.redis.RedisSessionUserRepository;
import com.terstredisproject1.usecase.user.port.RefreshUserSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshUserSessionPortImpl implements RefreshUserSessionPort {
    private final RedisSessionUserRepository redisSessionUserRepository;

    @Override
    public void refreshSession(long userId) {
        final boolean isSessionActive = redisSessionUserRepository.refreshSession(userId);
        if(!isSessionActive) {
            throw new RuntimeException("Error refreshing session");
        }
    }
}
