package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.RedisPaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.UpdatePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdatePaymentProfilePortImpl implements UpdatePaymentProfilePort {
    private final RedisPaymentProfileRepository redisPaymentProfileRepository;

    @Override
    public void update(long userId, UserPaymentProfile userPaymentProfile) {
        if (!redisPaymentProfileRepository.exists(userId)) {
            throw new IllegalArgumentException("User payment profile does not exist for userId: " + userId);
        }
        userPaymentProfile.setUserId(userId);
        redisPaymentProfileRepository.update(userPaymentProfile);
    }

    @Override
    public void updateStatus(long userId, PaymentStatus paymentStatus) {
        if (!redisPaymentProfileRepository.exists(userId)) {
            throw new IllegalArgumentException("User payment profile does not exist for userId: " + userId);
        }
        redisPaymentProfileRepository.updatePaymentStatus(userId, paymentStatus);
    }
}
