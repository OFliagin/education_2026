package com.terstredisproject1.adapter.controller.request.validation;

import com.terstredisproject1.adapter.controller.request.UpdateUserRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class UpdateUserValidatorImpl implements ConstraintValidator<UpdateUserValidation, UpdateUserRequest> {

    @Override
    public boolean isValid(UpdateUserRequest request, ConstraintValidatorContext context) {
        if (Objects.isNull(request)) {
            return true;
        }
        if (StringUtils.isBlank(request.name()) && StringUtils.isBlank(request.email())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Name or email should not be blank")
                    .addPropertyNode("name")
                    .addConstraintViolation();
            return false;
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
