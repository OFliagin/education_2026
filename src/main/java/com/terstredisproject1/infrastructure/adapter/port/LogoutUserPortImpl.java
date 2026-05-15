package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.infrastructure.db.redis.SessionUserRepository;
import com.terstredisproject1.usecase.user.port.LoginUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginUserPortImpl implements LoginUserPort {
    private final SessionUserRepository sessionUserRepository;
    @Override
    public void execute(User user) {
        sessionUserRepository.save(user);
    }
}
