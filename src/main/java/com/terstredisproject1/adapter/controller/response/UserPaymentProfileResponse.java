package com.terstredisproject1.adapter.controller.response;

public record UserPaymentProfileResponse(
        String plan,
        String currency,
        String paymentStatus,
        long balanceInCents,
        long lastPaymentAtEpochMillis,
        long nextBillingAtEpochMillis,
        int failedPaymentsCount
) {
}
