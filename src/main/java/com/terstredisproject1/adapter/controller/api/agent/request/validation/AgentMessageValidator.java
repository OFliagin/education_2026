package com.terstredisproject1.adapter.controller.api.agent.request.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgentMessageValidatorImpl.class)
public @interface AgentMessageValidator {
    String message() default "Invalid message structure request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
