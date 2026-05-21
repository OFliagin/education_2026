package com.terstredisproject1.usecase.user.port;

public interface SavePaymentResultPort {
    boolean profileExists(long userId);
    void recordSuccess(long userId, Long amountInCents);
    long recordFailure(long userId);
    void markPastDue(long userId);
}
