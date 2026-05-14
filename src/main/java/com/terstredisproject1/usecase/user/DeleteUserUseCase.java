package com.terstredisproject1.usecase.user;

import com.terstredisproject1.usecase.user.port.DeleteUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {
    private final DeleteUserPort deleteUserPort;

    public void execute(long userId) {
        deleteUserPort.deleteUser(userId);
    }
}
