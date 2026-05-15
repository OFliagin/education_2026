package com.terstredisproject1.adapter.controller.request.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoginUserValidatorImpl.class)
public @interface LoginUserValidator {
    String message() default "Invalid login user request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
