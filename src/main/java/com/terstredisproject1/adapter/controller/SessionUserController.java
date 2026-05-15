package com.terstredisproject1.adapter.controller;

import com.terstredisproject1.adapter.controller.request.LoginUserRequest;
import com.terstredisproject1.usecase.user.sassion.LoginUserUseCase;
import com.terstredisproject1.usecase.user.sassion.LogoutUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SessionUserController {
    private final LoginUserUseCase loginUserUseCase;
    private final LogoutUserUseCase logoutUserUseCase;

    @PostMapping("/login")
    public void login(@Valid @RequestBody LoginUserRequest loginUserRequest) {
        loginUserUseCase.execute(loginUserRequest);
    }

    @PostMapping("/logout/{userId}")
    public void logout(@PathVariable long userId) {
        logoutUserUseCase.execute(userId);
    }
}
