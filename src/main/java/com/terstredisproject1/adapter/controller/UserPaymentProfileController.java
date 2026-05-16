package com.terstredisproject1.adapter.controller;

import com.terstredisproject1.adapter.controller.request.SavePaymentResultRequest;
import com.terstredisproject1.adapter.controller.request.UpdatePaymentProfileRequest;
import com.terstredisproject1.adapter.controller.response.UserPaymentProfileResponse;
import com.terstredisproject1.usecase.user.profile.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        return createPaymentProfileUseCase.execute(userId);
    }

    @PutMapping("/payment-profile/users/{userId}")
    public void updatePaymentProfile(@PathVariable long userId, @RequestBody UpdatePaymentProfileRequest request) {
        updatePaymentProfileUseCase.update(userId, request);
    }

    @PutMapping("/payment-profile/users/{userId}/status")
    public void updatePaymentStatus(@PathVariable long userId, @RequestParam String paymentStatus) {
        updatePaymentProfileUseCase.updateStatus(userId, paymentStatus);
    }

    @GetMapping("/payment-profile/users/{userId}")
    public UserPaymentProfileResponse getPaymentProfile(@PathVariable long userId) {
        return getPaymentProfileUseCase.execute(userId);
    }

    @DeleteMapping("/payment-profile/users/{userId}")
    public void deletePaymentProfile(@PathVariable long userId) {
        deletePaymentProfileUseCase.execute(userId);
    }

    @PostMapping("/payment-profile/users/{userId}/payment-result")
    public void savePaymentResult(@PathVariable long userId, @RequestBody SavePaymentResultRequest request) {
        savePaymentResultUseCase.savePaymentResult(userId, request);
    }
}
