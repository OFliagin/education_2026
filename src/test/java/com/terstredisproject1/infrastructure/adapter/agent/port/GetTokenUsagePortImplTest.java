package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.PaymentPlan;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.domain.model.agent.TokenUsage;
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
class GetTokenUsagePortImplTest {

    private static final long USER_ID = 1L;
    private static final long AVAILABLE = PaymentPlan.BASIC.getAvailableTokens(); // 100

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Mock
    private RedisPaymentProfileRepository redisPaymentProfileRepository;

    @InjectMocks
    private GetTokenUsagePortImpl port;

    @Test
    void returns_correct_usage_when_partially_used() {
        givenTokenUsed(40L);
        givenBasicProfile();

        TokenUsage result = port.get(USER_ID);

        assertEquals(AVAILABLE, result.totalTokens());
        assertEquals(40L, result.usedTokens());
        assertEquals(60L, result.remainingTokens());
        assertEquals(40, result.usagePercent());
        assertFalse(result.limitExceeded());
    }

    @Test
    void returns_zero_remaining_and_full_percent_when_at_limit() {
        givenTokenUsed(AVAILABLE);
        givenBasicProfile();

        TokenUsage result = port.get(USER_ID);

        assertEquals(0L, result.remainingTokens());
        assertEquals(100, result.usagePercent());
        assertTrue(result.limitExceeded());
    }

    @Test
    void remaining_is_zero_not_negative_when_over_limit() {
        givenTokenUsed(AVAILABLE + 50);
        givenBasicProfile();

        TokenUsage result = port.get(USER_ID);

        assertEquals(0L, result.remainingTokens());
        assertEquals(100, result.usagePercent());
        assertTrue(result.limitExceeded());
    }

    @Test
    void returns_zero_usage_when_no_tokens_used() {
        givenTokenUsed(0L);
        givenBasicProfile();

        TokenUsage result = port.get(USER_ID);

        assertEquals(AVAILABLE, result.remainingTokens());
        assertEquals(0, result.usagePercent());
        assertFalse(result.limitExceeded());
    }

    @Test
    void throws_when_payment_profile_is_missing() {
        givenTokenUsed(0L);
        when(redisPaymentProfileRepository.findByUserId(USER_ID)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> port.get(USER_ID));
    }

    private void givenTokenUsed(long amount) {
        when(redisTokenRepository.getTokenUsage(USER_ID)).thenReturn(amount);
    }

    private void givenBasicProfile() {
        UserPaymentProfile profile = UserPaymentProfile.createDefault(USER_ID);
        when(redisPaymentProfileRepository.findByUserId(USER_ID)).thenReturn(profile);
    }
}