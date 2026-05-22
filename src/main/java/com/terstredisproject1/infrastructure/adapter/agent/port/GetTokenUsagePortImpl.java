package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.domain.model.agent.TokenUsage;
import com.terstredisproject1.infrastructure.db.redis.RedisPaymentProfileRepository;
import com.terstredisproject1.infrastructure.db.redis.RedisTokenRepository;
import com.terstredisproject1.usecase.agent.port.GetTokenUsagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetTokenUsagePortImpl implements GetTokenUsagePort {
    private final RedisTokenRepository redisTokenRepository;
    private final RedisPaymentProfileRepository redisPaymentProfileRepository;


    @Override
    public TokenUsage get(long userId) {
        log.info("Getting token usage for user: {}", userId);
        final long tokenUsage = redisTokenRepository.getTokenUsage(userId);
        final UserPaymentProfile paymentProfile = redisPaymentProfileRepository.findByUserId(userId);
        if (paymentProfile == null) {
            log.error("Payment profile not found for user: {}", userId);
            throw new IllegalStateException("Payment profile not found for user " + userId);
        }
        final long totalTokens = paymentProfile.getPlan().getAvailableTokens();
        final long remainingTokens = totalTokens - tokenUsage;
        final int usagePercent = (int) ((tokenUsage * 100.0) / totalTokens);
        final boolean limitExceeded = tokenUsage >= totalTokens;
        log.info("Token usage retrieved for user: {} - Available tokens: {}, Used tokens: {}, Usage percent: {}, Limit exceeded: {}", userId, totalTokens, tokenUsage, usagePercent, limitExceeded);
        return new TokenUsage(totalTokens, tokenUsage, usagePercent, remainingTokens, limitExceeded);
    }
}
