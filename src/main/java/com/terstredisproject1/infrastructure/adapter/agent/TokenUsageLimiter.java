package com.terstredisproject1.infrastructure.adapter.agent;

import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.RedisPaymentProfileRepository;
import com.terstredisproject1.infrastructure.db.redis.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenUsageLimiter {
    private final RedisTokenRepository redisTokenRepository;
    private final RedisPaymentProfileRepository redisPaymentProfileRepository;

    public boolean isTokenLimitExceeded(long userId, long tokenUsage) {
        long tokenUsed = redisTokenRepository.getTokenUsage(userId);

        UserPaymentProfile paymentProfile = redisPaymentProfileRepository.findByUserId(userId);
        if (paymentProfile == null) {
            throw new IllegalStateException("Payment profile not found for user " + userId);
        }

        if (paymentProfile.getPaymentStatus() != PaymentStatus.ACTIVE) {
            throw new IllegalStateException("Payment profile is not active for user " + userId);
        }

        long availableTokens = paymentProfile.getPlan().getAvailableTokens();
        return tokenUsed >= availableTokens || tokenUsage + tokenUsed > availableTokens;
    }
}
