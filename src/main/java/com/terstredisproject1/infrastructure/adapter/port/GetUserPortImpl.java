package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.infrastructure.db.UserRepository;
import com.terstredisproject1.usecase.user.port.GetUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserPortImpl implements GetUserPort {

    private final UserRepository userRepository;

    @Override
    public User getUser(long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
