package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.PaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.UpdatePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UpdatePaymentProfilePortImpl implements UpdatePaymentProfilePort {
    private final PaymentProfileRepository paymentProfileRepository;

    @Override
    public void update(long userId, UserPaymentProfile userPaymentProfile) {
        if (!paymentProfileRepository.exists(userId)) {
            throw new IllegalArgumentException("User payment profile does not exist for userId: " + userId);
        }
        userPaymentProfile.setUserId(userId);
        paymentProfileRepository.update(userPaymentProfile);
    }



    @Override
    public void updateStatus(long userId, PaymentStatus paymentStatus) {
        if (!paymentProfileRepository.exists(userId)) {
            throw new IllegalArgumentException("User payment profile does not exist for userId: " + userId);
        }
        paymentProfileRepository.updatePaymentStatus(userId, paymentStatus);
    }
}
