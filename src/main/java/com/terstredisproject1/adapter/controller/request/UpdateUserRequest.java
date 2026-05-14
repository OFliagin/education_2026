package com.terstredisproject1.adapter.controller.request;

import com.terstredisproject1.adapter.controller.request.validation.UpdateUserValidation;
import jakarta.validation.constraints.Email;

@UpdateUserValidation
public record UpdateUserRequest(
                                String name,
                                @Email(message = "Invalid email format") String email) {
}
