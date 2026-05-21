package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.usecase.user.port.GetPaymentProfilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetPaymentProfileUseCase {
    private final GetPaymentProfilePort getPaymentProfilePort;

    public UserPaymentProfile execute(long userId) {
        return getPaymentProfilePort.execute(userId).orElseThrow();
    }
}
