package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.adapter.controller.request.UpdatePaymentProfileRequest;
import com.terstredisproject1.domain.model.PaymentPlan;
import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.usecase.user.port.UpdatePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdatePaymentProfileUseCase {
    private final UpdatePaymentProfilePort updatePaymentProfilePort;

    public void update(long userId, UpdatePaymentProfileRequest paymentProfileId) {
        updatePaymentProfilePort.update(userId, mapToPaymentProfile(paymentProfileId));
        log.info("Payment profile updated for user: {}", userId);
    }

    public void updateStatus(long userId, String paymentStatus) {
        updatePaymentProfilePort.updateStatus(userId, PaymentStatus.valueOf(paymentStatus));
    }

    private UserPaymentProfile mapToPaymentProfile(UpdatePaymentProfileRequest request) {
            return UserPaymentProfile.builder()
                    .plan(PaymentPlan.valueOf(request.plan()))
                    .currency(request.currency())
                    .paymentStatus(PaymentStatus.valueOf(request.paymentStatus()))
                    .balanceInCents(request.balanceInCents())
                    .build();
    }
}
