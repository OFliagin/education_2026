package com.terstredisproject1.adapter.controller;

import com.terstredisproject1.adapter.controller.request.LoginUserRequest;
import com.terstredisproject1.usecase.user.session.CheckUserSessionUseCase;
import com.terstredisproject1.usecase.user.session.LoginUserUseCase;
import com.terstredisproject1.usecase.user.session.LogoutUserUseCase;
import com.terstredisproject1.usecase.user.session.RefreshUserSessionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SessionUserController {
    private final LoginUserUseCase loginUserUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final CheckUserSessionUseCase checkUserSessionUseCase;
    private final RefreshUserSessionUseCase refreshUserSessionUseCase;

    @PostMapping("/auth/login")
    public void login(@Valid @RequestBody LoginUserRequest loginUserRequest) {
        loginUserUseCase.execute(loginUserRequest);
    }

    @PostMapping("/auth/logout/{userId}")
    public void logout(@PathVariable long userId) {
        logoutUserUseCase.execute(userId);
    }

    @GetMapping("/auth/online/{userId}")
    public boolean isUserOnline(@PathVariable long userId) {
        return checkUserSessionUseCase.isUserOnline(userId);
    }

    @PostMapping("/auth/refresh/{userId}")
    public void refreshSession(@PathVariable long userId) {
        refreshUserSessionUseCase.execute(userId);
    }
}
