package com.terstredisproject1.adapter.controller;

import com.terstredisproject1.adapter.controller.request.LoginUserRequest;
import com.terstredisproject1.usecase.user.login.LoginUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginUserController {
private final LoginUserUseCase loginUserUseCase;

    @PostMapping("/login")
    public void login(@RequestBody LoginUserRequest loginUserRequest) {
        loginUserUseCase.execute(loginUserRequest);
    }
}
