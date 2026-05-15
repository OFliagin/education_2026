package com.terstredisproject1.adapter.controller.request.validation;

import com.terstredisproject1.adapter.controller.request.LoginUserRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class LoginUserValidatorImpl implements ConstraintValidator<LoginUserValidator, LoginUserRequest> {
    @Override
    public boolean isValid(LoginUserRequest request, ConstraintValidatorContext context) {
        if (Objects.isNull(request)) {
            return true;
        }
        if (StringUtils.isNoneBlank(request.email()) && StringUtils.isNoneBlank(request.password()) && request.password().contains(request.email())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password should not contain email")
                    .addPropertyNode("password")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
