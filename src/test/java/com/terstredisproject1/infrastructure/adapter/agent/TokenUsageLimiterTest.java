package com.terstredisproject1.infrastructure.adapter.agent;

import com.terstredisproject1.domain.model.PaymentPlan;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.RedisPaymentProfileRepository;
import com.terstredisproject1.infrastructure.db.redis.RedisTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenUsageLimiterTest {

    private static final long USER_ID = 1L;
    // BASIC plan has 100 available tokens
    private static final long AVAILABLE = PaymentPlan.BASIC.getAvailableTokens();

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Mock
    private RedisPaymentProfileRepository redisPaymentProfileRepository;

    @InjectMocks
    private TokenUsageLimiter tokenUsageLimiter;

    @Test
    void not_exceeded_when_usage_and_cost_are_within_limit() {
        long tokenUsed = 50L;
        long cost = 30L;
        givenTokenUsed(tokenUsed);
        givenBasicProfile();

        assertFalse(tokenUsageLimiter.isTokenLimitExceeded(USER_ID, cost));
    }

    @Test
    void exceeded_when_already_used_equals_available() {
        givenTokenUsed(AVAILABLE);
        givenBasicProfile();

        assertTrue(tokenUsageLimiter.isTokenLimitExceeded(USER_ID, 0L));
    }

    @Test
    void exceeded_when_cost_would_push_over_limit() {
        long tokenUsed = 90L;
        long cost = 20L; // 90 + 20 = 110 > 100
        givenTokenUsed(tokenUsed);
        givenBasicProfile();

        assertTrue(tokenUsageLimiter.isTokenLimitExceeded(USER_ID, cost));
    }

    @Test
    void not_exceeded_when_cost_exactly_fills_remaining_tokens() {
        long tokenUsed = 90L;
        long cost = 10L; // 90 + 10 = 100 == 100, not strictly greater
        givenTokenUsed(tokenUsed);
        givenBasicProfile();

        assertFalse(tokenUsageLimiter.isTokenLimitExceeded(USER_ID, cost));
    }

    @Test
    void exceeded_when_already_at_limit_and_zero_cost() {
        givenTokenUsed(AVAILABLE);
        givenBasicProfile();

        assertTrue(tokenUsageLimiter.isTokenLimitExceeded(USER_ID, 0L));
    }

    @Test
    void throws_when_payment_profile_is_missing() {
        givenTokenUsed(0L);
        when(redisPaymentProfileRepository.findByUserId(USER_ID)).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> tokenUsageLimiter.isTokenLimitExceeded(USER_ID, 10L));
    }

    private void givenTokenUsed(long amount) {
        when(redisTokenRepository.getTokenUsage(USER_ID)).thenReturn(amount);
    }

    private void givenBasicProfile() {
        UserPaymentProfile profile = UserPaymentProfile.createDefault(USER_ID);
        when(redisPaymentProfileRepository.findByUserId(USER_ID)).thenReturn(profile);
    }
}