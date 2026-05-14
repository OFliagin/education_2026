package com.terstredisproject1.usecase.user;

import com.terstredisproject1.adapter.controller.request.UpdateUserRequest;
import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.usecase.user.port.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {
    private final UpdateUserPort updateUserPort;


    public void execute(long id, UpdateUserRequest updateUserRequest) {
        updateUserPort.updateUser(id, mapToUser(updateUserRequest));
    }

    private User mapToUser(UpdateUserRequest updateUserRequest) {
        return User.builder()
                .username(updateUserRequest.name())
                .email(updateUserRequest.email())
                .build();
    }
}
