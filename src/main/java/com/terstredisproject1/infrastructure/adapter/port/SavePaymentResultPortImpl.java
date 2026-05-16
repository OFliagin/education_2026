package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.PaymentProcessStatus;
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
    public void savePaymentResult(long userId, Long amountInCents, PaymentProcessStatus status) {
        if (!redisPaymentProfileRepository.exists(userId)) {
            throw new IllegalArgumentException("User payment profile does not exist for userId: " + userId);
        }
        if (status == PaymentProcessStatus.SUCCESS) {
            redisPaymentProfileRepository.update(UserPaymentProfile.updateAfterSucceed(userId, amountInCents));
        } else {
            final long failedCount = redisPaymentProfileRepository.incrementFailedPayments(userId);

            if (failedCount >= 3) {
                redisPaymentProfileRepository.updatePaymentStatus(userId, PaymentStatus.PAST_DUE);
            }
        }
    }
}
