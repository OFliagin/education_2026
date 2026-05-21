package com.terstredisproject1.usecase.user.session;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.usecase.user.port.GetUserPort;
import com.terstredisproject1.usecase.user.port.LoginUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginUserUseCase {
    private final GetUserPort getUserPort;
    private final LoginUserPort loginUserPort;

    public void execute(String email, String password) {
        final User userByEmail = getUserPort.getUserByEmail(email);
        if (userByEmail == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }

        if (!password.equals(userByEmail.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        loginUserPort.execute(userByEmail);
        log.info("User with email {} logged in successfully", email);
    }
}
