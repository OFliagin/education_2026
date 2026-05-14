package com.terstredisproject1.usecase.user;

import com.terstredisproject1.adapter.controller.response.GetUserResponse;
import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.usecase.user.port.GetUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetUserUseCase {
    private final GetUserPort getUserPort;

    public GetUserResponse execute(long userId) {
        final User user = getUserPort.getUser(userId);
        if (user == null) {
            log.warn("User not found with id: {}", userId);
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        return new GetUserResponse(user.getUsername(), user.getEmail());
    }
}
