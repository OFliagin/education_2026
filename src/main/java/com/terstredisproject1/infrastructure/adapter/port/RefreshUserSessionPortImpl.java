package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.infrastructure.db.redis.SessionUserRepository;
import com.terstredisproject1.usecase.user.port.RefreshUserSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshUserSessionPortImpl implements RefreshUserSessionPort {
    private final SessionUserRepository sessionUserRepository;

    @Override
    public void refreshSession(long userId) {
        final boolean isSessionActive = sessionUserRepository.refreshSession(userId);
        if(!isSessionActive) {
            throw new RuntimeException("Error refreshing session");
        }
    }
}
