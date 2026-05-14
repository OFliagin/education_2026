package com.terstredisproject1.adapter.controller.request.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateUserValidatorImpl.class)
public @interface CreateUserValidator {
    String message() default "Invalid create user request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
