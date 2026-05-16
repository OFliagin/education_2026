package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.infrastructure.db.redis.RedisSessionUserRepository;
import com.terstredisproject1.usecase.user.port.LogoutUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogoutUserPortImpl implements LogoutUserPort {
    private final RedisSessionUserRepository redisSessionUserRepository;
    @Override
    public void execute(User user) {
        redisSessionUserRepository.delete(user);
    }
}
