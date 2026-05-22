package com.terstredisproject1.infrastructure.adapter.agent;

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


    public boolean isTokenUsageExceeded(long userId, long tokenUsage) {
        val tokenUsed = redisTokenRepository.getTokenUsage(userId);
        val availableTokens = redisPaymentProfileRepository.findByUserId(userId).getPlan().getAvailableTokens();
        return tokenUsed >= availableTokens || tokenUsage + tokenUsed > availableTokens;
    }
}
