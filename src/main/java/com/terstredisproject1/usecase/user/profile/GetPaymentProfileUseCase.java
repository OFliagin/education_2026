package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.adapter.controller.response.UserPaymentProfileResponse;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.usecase.user.port.GetPaymentProfilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetPaymentProfileUseCase {
    private final GetPaymentProfilePort getPaymentProfilePort;

    public UserPaymentProfileResponse execute(long userId) {
        return getPaymentProfilePort.execute(userId)
                .map(this::mapToResponse)
                .orElseThrow();
    }

    private UserPaymentProfileResponse mapToResponse(UserPaymentProfile execute) {
        return new UserPaymentProfileResponse(
                execute.getPlan().name(),
                execute.getCurrency(),
                execute.getPaymentStatus().name(),
                execute.getBalanceInCents(),
                execute.getLastPaymentAtEpochMillis(),
                execute.getNextBillingAtEpochMillis(),
                execute.getFailedPaymentsCount()
        );
    }
}
