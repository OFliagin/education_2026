package com.terstredisproject1.usecase.user;

import com.terstredisproject1.adapter.controller.request.CreateUserRequest;
import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.usecase.user.port.CreateUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateUserUseCase {
    private final CreateUserPort createUserPort;

    public long execute(CreateUserRequest createUserRequest) {
        log.debug("Creating user with request: {}", createUserRequest);
        User createdUser = createUserPort.createUser(mapToUser(createUserRequest));
        if (createdUser != null) {
            return createdUser.getId();
        } else {
            log.error("Failed to create user with request: {}", createUserRequest);
            throw new RuntimeException("Failed to create user");
        }
    }

    private User mapToUser(CreateUserRequest createUserRequest) {
        return User.builder()
                .username(createUserRequest.name())
                .email(createUserRequest.email())
                .password(createUserRequest.password())
                .build();
    }
}
