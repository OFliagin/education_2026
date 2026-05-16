package com.terstredisproject1.usecase.user.session;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.usecase.user.port.GetUserPort;
import com.terstredisproject1.usecase.user.port.LogoutUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutUserUseCase {
    private final GetUserPort getUserPort;
    private final LogoutUserPort logoutUserPort;

    public void execute(long userId) {
        User user = getUserPort.getUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        logoutUserPort.execute(user);
        log.info("User with id {} logged out successfully", userId);
    }
}
