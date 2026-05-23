package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.exception.TokenLimitExceeded;
import com.terstredisproject1.domain.exception.TokenLockException;
import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.infrastructure.adapter.agent.TokenUsageLimiter;
import com.terstredisproject1.infrastructure.client.AiAgentClient;
import com.terstredisproject1.infrastructure.db.redis.RedisTokenRepository;
import com.terstredisproject1.infrastructure.locker.Locker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMessagePortImplTest {

    private static final long USER_ID = 1L;
    private static final String USER_ID_STR = "1";

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Mock
    private TokenUsageLimiter tokenUsageLimiter;

    @Mock
    private AiAgentClient aiAgentClient;

    @Mock
    private Locker locker;

    @InjectMocks
    private GetMessagePortImpl port;
    // isUseImitationProcess defaults to false — no sleep in tests

    @Test
    void returns_prompt_and_skips_lock_when_message_is_blank() {
        AgentResult result = port.execute(USER_ID, "   ");

        assertEquals("Please ask a question", result.message());
        verify(locker, never()).lock(anyString(), any(UUID.class));
    }

    @Test
    void throws_token_lock_exception_when_lock_is_not_acquired() {
        when(locker.lock(eq(USER_ID_STR), any(UUID.class))).thenReturn(false);

        assertThrows(TokenLockException.class, () -> port.execute(USER_ID, "hello"));
    }

    @Test
    void throws_token_limit_exceeded_and_releases_lock() {
        when(locker.lock(eq(USER_ID_STR), any(UUID.class))).thenReturn(true);
        doThrow(new TokenLimitExceeded("limit exceeded"))
                .when(tokenUsageLimiter).isTokenLimitExceeded(eq(USER_ID), anyLong());

        assertThrows(TokenLimitExceeded.class, () -> port.execute(USER_ID, "hello"));

        verify(locker).unlock(eq(USER_ID_STR), any(UUID.class));
    }

    @Test
    void releases_lock_in_finally_when_ai_client_throws() {
        when(locker.lock(eq(USER_ID_STR), any(UUID.class))).thenReturn(true);
        when(tokenUsageLimiter.isTokenLimitExceeded(eq(USER_ID), anyLong())).thenReturn(false);
        when(aiAgentClient.ask(anyString())).thenThrow(new RuntimeException("Ollama timeout"));

        assertThrows(RuntimeException.class, () -> port.execute(USER_ID, "hello"));

        verify(locker).unlock(eq(USER_ID_STR), any(UUID.class));
        verify(redisTokenRepository, never()).incrementTokenUsage(anyLong(), anyLong());
    }

    @Test
    void happy_path_returns_agent_reply_and_increments_tokens() {
        String message = "hello";
        when(locker.lock(eq(USER_ID_STR), any(UUID.class))).thenReturn(true);
        when(tokenUsageLimiter.isTokenLimitExceeded(eq(USER_ID), anyLong())).thenReturn(false);
        when(aiAgentClient.ask(message)).thenReturn("world");

        AgentResult result = port.execute(USER_ID, message);

        assertEquals("world", result.message());
        verify(redisTokenRepository).incrementTokenUsage(anyLong(), eq(USER_ID));
        verify(locker).unlock(eq(USER_ID_STR), any(UUID.class));
    }

    @Test
    void calculates_token_cost_as_10_for_message_up_to_50_chars() {
        String message = "a".repeat(50);
        when(locker.lock(eq(USER_ID_STR), any(UUID.class))).thenReturn(true);
        when(tokenUsageLimiter.isTokenLimitExceeded(eq(USER_ID), anyLong())).thenReturn(false);
        when(aiAgentClient.ask(message)).thenReturn("ok");

        port.execute(USER_ID, message);

        verify(redisTokenRepository).incrementTokenUsage(eq(10L), eq(USER_ID));
    }

    @Test
    void calculates_token_cost_as_20_for_message_of_51_chars() {
        String message = "a".repeat(51);
        when(locker.lock(eq(USER_ID_STR), any(UUID.class))).thenReturn(true);
        when(tokenUsageLimiter.isTokenLimitExceeded(eq(USER_ID), anyLong())).thenReturn(false);
        when(aiAgentClient.ask(message)).thenReturn("ok");

        port.execute(USER_ID, message);

        verify(redisTokenRepository).incrementTokenUsage(eq(20L), eq(USER_ID));
    }
}