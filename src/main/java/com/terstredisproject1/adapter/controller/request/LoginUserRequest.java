package com.terstredisproject1.adapter.controller.request;

import com.terstredisproject1.adapter.controller.request.validation.LoginUserValidator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@LoginUserValidator
public record LoginUserRequest (
        @NotBlank(message = "Email is required") @Email String email,
        @NotBlank(message = "Password is required") String password) {
}
