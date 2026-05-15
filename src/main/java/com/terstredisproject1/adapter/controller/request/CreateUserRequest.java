package com.terstredisproject1.adapter.controller.request;

import com.terstredisproject1.adapter.controller.request.validation.CreateUserValidator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@CreateUserValidator
public record CreateUserRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") String password
) {
}
