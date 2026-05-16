package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;

public interface UpdatePaymentProfilePort {
    void update(long userId, UserPaymentProfile userPaymentProfile);
    void updateStatus(long userId, PaymentStatus paymentStatus);
}
