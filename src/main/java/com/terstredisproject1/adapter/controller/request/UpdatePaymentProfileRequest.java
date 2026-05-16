package com.terstredisproject1.adapter.controller.request;

public record UpdatePaymentProfileRequest(
        String plan,
        String currency,
        String paymentStatus,
        Long balanceInCents
) {
}
