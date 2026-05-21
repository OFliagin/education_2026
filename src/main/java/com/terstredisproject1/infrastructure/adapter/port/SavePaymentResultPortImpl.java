package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.RedisPaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.SavePaymentResultPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavePaymentResultPortImpl implements SavePaymentResultPort {
    private final RedisPaymentProfileRepository redisPaymentProfileRepository;

    @Override
    public boolean profileExists(long userId) {
        return redisPaymentProfileRepository.exists(userId);
    }

    @Override
    public void recordSuccess(long userId, Long amountInCents) {
        redisPaymentProfileRepository.update(UserPaymentProfile.updateAfterSucceed(userId, amountInCents));
    }

    @Override
    public long recordFailure(long userId) {
        return redisPaymentProfileRepository.incrementFailedPayments(userId);
    }

    @Override
    public void markPastDue(long userId) {
        redisPaymentProfileRepository.updatePaymentStatus(userId, PaymentStatus.PAST_DUE);
    }
}
