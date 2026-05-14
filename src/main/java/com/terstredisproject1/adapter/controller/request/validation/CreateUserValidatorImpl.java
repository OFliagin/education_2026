package com.terstredisproject1.adapter.controller.request.validation;

import com.terstredisproject1.adapter.controller.request.CreateUserRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class CreateUserValidatorImpl implements ConstraintValidator<CreateUserValidator, CreateUserRequest> {

    @Override
    public boolean isValid(CreateUserRequest request, ConstraintValidatorContext context) {
        if (Objects.isNull(request)) {
            return true;
        }
        if (StringUtils.isNoneBlank(request.name()) && StringUtils.isNoneBlank(request.email()) && request.name().contains(request.email())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Name should not contain email")
                    .addPropertyNode("name")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
