package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.infrastructure.db.UserRepository;
import com.terstredisproject1.usecase.user.port.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateUserPortImpl implements UpdateUserPort {
    private final UserRepository userRepository;

    @Override
    public void updateUser(long id, User user) {
        userRepository.findById(id)
                .ifPresentOrElse(
                        existingUser -> {
                            if (user.getUsername() != null) {
                                existingUser.setUsername(user.getUsername());
                            }
                            if (user.getEmail() != null) {
                                existingUser.setEmail(user.getEmail());
                            }
                            userRepository.save(existingUser);
                        },
                        () -> {
                            throw new IllegalArgumentException("User with ID " + id + " not found");
                        }
                );
    }
}
