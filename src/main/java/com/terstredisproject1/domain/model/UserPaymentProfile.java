package com.terstredisproject1.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@Builder
public class UserPaymentProfile {
    private long userId;
    private PaymentPlan plan;
    private String currency;
    private PaymentStatus paymentStatus;
    private Long balanceInCents;
    private Long lastPaymentAtEpochMillis;
    private Long nextBillingAtEpochMillis;
    private Integer failedPaymentsCount;

    public static UserPaymentProfile createDefault(long userId) {
        final Instant now = Instant.now();
        return UserPaymentProfile.builder()
                .userId(userId)
                .plan(PaymentPlan.BASIC)
                .paymentStatus(PaymentStatus.ACTIVE)
                .balanceInCents(0L)
                .lastPaymentAtEpochMillis(now.toEpochMilli())
                .nextBillingAtEpochMillis(now.plusMillis(TimeUnit.DAYS.toMillis(30)).toEpochMilli())
                .failedPaymentsCount(0)
                .currency("USD")
                .build();
    }


    public static UserPaymentProfile updateAfterSucceed(long userId, long amountInCents) {
        final Instant now = Instant.now();
        return UserPaymentProfile.builder()
                .userId(userId)
                .paymentStatus(PaymentStatus.ACTIVE)
                .balanceInCents(amountInCents)
                .lastPaymentAtEpochMillis(now.toEpochMilli())
                .nextBillingAtEpochMillis(now.plusMillis(TimeUnit.DAYS.toMillis(30)).toEpochMilli())
                .failedPaymentsCount(0)
                .build();
    }
}
