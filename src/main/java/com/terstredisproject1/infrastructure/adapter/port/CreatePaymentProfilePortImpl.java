package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.PaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.CreatePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePaymentProfilePortImpl implements CreatePaymentProfilePort {
    private final PaymentProfileRepository paymentProfileRepository;

    @Override
    public UserPaymentProfile execute(long userId) {
        if (paymentProfileRepository.exists(userId)) {
            throw new IllegalArgumentException("Payment profile already exists for user: " + userId);
        }
        UserPaymentProfile userPaymentProfile = UserPaymentProfile.createDefault(userId);
        paymentProfileRepository.save(userPaymentProfile);
        log.info("Payment profile created for user: {}", userId);
        return userPaymentProfile;
    }
}
