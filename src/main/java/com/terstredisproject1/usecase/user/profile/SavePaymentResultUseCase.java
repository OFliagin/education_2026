package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.domain.model.PaymentProcessStatus;
import com.terstredisproject1.usecase.user.port.SavePaymentResultPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SavePaymentResultUseCase {
    private static final int PAST_DUE_THRESHOLD = 3;

    private final SavePaymentResultPort savePaymentResultPort;

    public void savePaymentResult(long userId, Long amountInCents, PaymentProcessStatus status) {
        if (!savePaymentResultPort.profileExists(userId)) {
            throw new IllegalArgumentException("Payment profile does not exist for userId: " + userId);
        }
        log.info("Saving payment result for user: {} with status: {}", userId, status);
        if (status == PaymentProcessStatus.SUCCESS) {
            savePaymentResultPort.recordSuccess(userId, amountInCents);
        } else {
            final long failedCount = savePaymentResultPort.recordFailure(userId);
            if (failedCount >= PAST_DUE_THRESHOLD) {
                log.warn("User {} reached {} failed payments, marking PAST_DUE", userId, failedCount);
                savePaymentResultPort.markPastDue(userId);
            }
        }
    }
}
