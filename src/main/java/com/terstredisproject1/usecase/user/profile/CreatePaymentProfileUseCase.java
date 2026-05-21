package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.usecase.user.port.CreatePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatePaymentProfileUseCase {
    private final CreatePaymentProfilePort createPaymentProfilePort;

    public UserPaymentProfile execute(long userId) {
        return createPaymentProfilePort.execute(userId);
    }
}
