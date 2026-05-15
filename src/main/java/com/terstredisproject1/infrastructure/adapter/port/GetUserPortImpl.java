package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.infrastructure.db.UserRepository;
import com.terstredisproject1.usecase.user.port.GetUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetUserPortImpl implements GetUserPort {

    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public User getUser(long userId) {
        log.debug("Getting user with id: {}", userId);
        return userRepository.findById(userId).orElse(null);
    }
}
