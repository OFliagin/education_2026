package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.usecase.user.port.UpdatePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdatePaymentProfileUseCase {
    private final UpdatePaymentProfilePort updatePaymentProfilePort;

    public void update(long userId, UserPaymentProfile profile) {
        updatePaymentProfilePort.update(userId, profile);
        log.info("Payment profile updated for user: {}", userId);
    }

    public void updateStatus(long userId, PaymentStatus status) {
        log.info("Updating payment status for user: {} with status: {}", userId, status);
        updatePaymentProfilePort.updateStatus(userId, status);
    }
}
