package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.infrastructure.db.pg.UserRepository;
import com.terstredisproject1.usecase.user.port.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateUserPortImpl implements UpdateUserPort {
    private final UserRepository userRepository;

    @Override
    @CachePut(value = "users", key = "#id", unless = "#result == null")
    public User updateUser(long id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (user.getUsername() != null) {
                        existingUser.setUsername(user.getUsername());
                    }
                    if (user.getEmail() != null) {
                        existingUser.setEmail(user.getEmail());
                    }
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found"));
    }
}
