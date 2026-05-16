package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.infrastructure.db.redis.SessionUserRepository;
import com.terstredisproject1.usecase.user.port.CheckUserSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckUserSessionPortImpl implements CheckUserSessionPort {
    private final SessionUserRepository sessionUserRepository;

    @Override
    public boolean isUserOnline(long userId) {
        return sessionUserRepository.isUserOnline(userId);
    }
}
