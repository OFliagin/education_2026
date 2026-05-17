package com.terstredisproject1.adapter.controller.api.agent.request.validation;

import com.terstredisproject1.adapter.controller.api.agent.request.AgentRequest;
import com.terstredisproject1.adapter.controller.request.CreateUserRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class AgentMessageValidatorImpl implements ConstraintValidator<AgentMessageValidator, AgentRequest> {

    @Override
    public boolean isValid(AgentRequest request, ConstraintValidatorContext context) {
        if (Objects.isNull(request)) {
            return true;
        }
        if (StringUtils.isNoneBlank(request.message()) && (request.message().length() > 50 || request.message().length() < 10 )) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Message length should be between 10 and 50 characters")
                    .addPropertyNode("message")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
