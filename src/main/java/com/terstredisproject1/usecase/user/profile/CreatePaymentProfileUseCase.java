package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.adapter.controller.response.UserPaymentProfileResponse;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.usecase.user.port.CreatePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatePaymentProfileUseCase {
    private final CreatePaymentProfilePort createPaymentProfilePort;

    public UserPaymentProfileResponse execute(long userId) {
        final UserPaymentProfile userPaymentProfile = createPaymentProfilePort.execute(userId);
        return mapToResponse(userPaymentProfile);
    }

    private UserPaymentProfileResponse mapToResponse(UserPaymentProfile paymentProfile) {
        return new UserPaymentProfileResponse(
                paymentProfile.getPlan().name(),
                paymentProfile.getCurrency(),
                paymentProfile.getPaymentStatus().name(),
                paymentProfile.getBalanceInCents(),
                paymentProfile.getLastPaymentAtEpochMillis(),
                paymentProfile.getNextBillingAtEpochMillis(),
                paymentProfile.getFailedPaymentsCount()
        );
    }
}
