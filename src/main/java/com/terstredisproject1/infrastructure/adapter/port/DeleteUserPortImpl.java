package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.infrastructure.db.pg.UserRepository;
import com.terstredisproject1.usecase.user.port.DeleteUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUserPortImpl implements DeleteUserPort {
    private final UserRepository userRepository;

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }
}
