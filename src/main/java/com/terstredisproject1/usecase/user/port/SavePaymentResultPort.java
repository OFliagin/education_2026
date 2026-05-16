package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.PaymentProcessStatus;

public interface SavePaymentResultPort {
    void savePaymentResult(long userId, Long amountInCents, PaymentProcessStatus status);
}
