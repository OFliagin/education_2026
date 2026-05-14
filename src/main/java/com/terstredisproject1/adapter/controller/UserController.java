package com.terstredisproject1.adapter.controller;

import com.terstredisproject1.adapter.controller.request.CreateUserRequest;
import com.terstredisproject1.adapter.controller.request.UpdateUserRequest;
import com.terstredisproject1.adapter.controller.response.GetUserResponse;
import com.terstredisproject1.usecase.user.GetUserUseCase;
import com.terstredisproject1.usecase.user.CreateUserUseCase;
import com.terstredisproject1.usecase.user.DeleteUserUseCase;
import com.terstredisproject1.usecase.user.UpdateUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    @PostMapping("/users")
    public long createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        log.debug("Creating user with request: {}", createUserRequest);
        return createUserUseCase.execute(createUserRequest);
    }

    @PatchMapping("/users/{id}")
    public void updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        log.debug("Updating user with id {} and request: {}", id, updateUserRequest);
        updateUserUseCase.execute(id, updateUserRequest);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.debug("Deleting user with id {}", id);
        deleteUserUseCase.execute(id);
    }

    @GetMapping("/users/{id}")
    public GetUserResponse getUser(@PathVariable Long id) {
        log.debug("Getting user with id {}", id);
        return getUserUseCase.execute(id);
    }

}
