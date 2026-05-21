package com.terstredisproject1.adapter.controller;

import com.terstredisproject1.adapter.controller.request.SavePaymentResultRequest;
import com.terstredisproject1.adapter.controller.request.UpdatePaymentProfileRequest;
import com.terstredisproject1.adapter.controller.response.UserPaymentProfileResponse;
import com.terstredisproject1.domain.model.PaymentPlan;
import com.terstredisproject1.domain.model.PaymentProcessStatus;
import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.usecase.user.profile.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class UserPaymentProfileController {
    private final GetPaymentProfileUseCase getPaymentProfileUseCase;
    private final UpdatePaymentProfileUseCase updatePaymentProfileUseCase;
    private final CreatePaymentProfileUseCase createPaymentProfileUseCase;
    private final DeletePaymentProfileUseCase deletePaymentProfileUseCase;
    private final SavePaymentResultUseCase savePaymentResultUseCase;

    @PostMapping("/payment-profile/users/{userId}")
    public UserPaymentProfileResponse createPaymentProfile(@PathVariable long userId) {
        return toResponse(createPaymentProfileUseCase.execute(userId));
    }

    @PutMapping("/payment-profile/users/{userId}")
    public void updatePaymentProfile(@PathVariable long userId, @RequestBody UpdatePaymentProfileRequest request) {
        updatePaymentProfileUseCase.update(userId, toProfile(request));
    }

    @PutMapping("/payment-profile/users/{userId}/status")
    public void updatePaymentStatus(@PathVariable long userId, @RequestParam String paymentStatus) {
        updatePaymentProfileUseCase.updateStatus(userId, PaymentStatus.valueOf(paymentStatus));
    }

    @GetMapping("/payment-profile/users/{userId}")
    public UserPaymentProfileResponse getPaymentProfile(@PathVariable long userId) {
        return toResponse(getPaymentProfileUseCase.execute(userId));
    }

    @DeleteMapping("/payment-profile/users/{userId}")
    public void deletePaymentProfile(@PathVariable long userId) {
        deletePaymentProfileUseCase.execute(userId);
    }

    @PostMapping("/payment-profile/users/{userId}/payment-result")
    public void savePaymentResult(@PathVariable long userId, @RequestBody SavePaymentResultRequest request) {
        savePaymentResultUseCase.savePaymentResult(
                userId,
                request.amountInCents(),
                PaymentProcessStatus.valueOf(request.status())
        );
    }

    private UserPaymentProfileResponse toResponse(UserPaymentProfile p) {
        return new UserPaymentProfileResponse(
                p.getPlan().name(),
                p.getCurrency(),
                p.getPaymentStatus().name(),
                p.getBalanceInCents(),
                p.getLastPaymentAtEpochMillis(),
                p.getNextBillingAtEpochMillis(),
                p.getFailedPaymentsCount()
        );
    }

    private UserPaymentProfile toProfile(UpdatePaymentProfileRequest request) {
        return UserPaymentProfile.builder()
                .plan(Objects.nonNull(request.plan()) ? PaymentPlan.valueOf(request.plan()) : null)
                .currency(request.currency())
                .paymentStatus(Objects.nonNull(request.paymentStatus()) ? PaymentStatus.valueOf(request.paymentStatus()) : null)
                .balanceInCents(request.balanceInCents())
                .build();
    }
}
