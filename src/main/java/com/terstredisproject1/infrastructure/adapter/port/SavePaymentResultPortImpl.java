package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.PaymentProcessStatus;
import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.PaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.SavePaymentResultPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavePaymentResultPortImpl implements SavePaymentResultPort {
    private final PaymentProfileRepository paymentProfileRepository;

    @Override
    public void savePaymentResult(long userId, Long amountInCents, PaymentProcessStatus status) {
        if (!paymentProfileRepository.exists(userId)) {
            throw new IllegalArgumentException("User payment profile does not exist for userId: " + userId);
        }
        if (status == PaymentProcessStatus.SUCCESS) {
            paymentProfileRepository.update(UserPaymentProfile.updateAfterSucceed(userId, amountInCents));
        } else {
            final long failedCount = paymentProfileRepository.incrementFailedPayments(userId);

            if (failedCount >= 3) {
                paymentProfileRepository.updatePaymentStatus(userId, PaymentStatus.PAST_DUE);
            }
        }
    }
}
