package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.exception.TokenLimitExceeded;
import com.terstredisproject1.domain.exception.TokenLockException;
import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.infrastructure.adapter.agent.TokenUsageLimiter;
import com.terstredisproject1.infrastructure.client.AiAgentClient;
import com.terstredisproject1.infrastructure.db.redis.RedisTokenRepository;
import com.terstredisproject1.infrastructure.locker.Locker;
import com.terstredisproject1.usecase.agent.port.GetMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetMessagePortImpl implements GetMessagePort {
    @Value("${ai:is.use.imitation.process:true}")
    private boolean isUseImitationProcess;

    private final RedisTokenRepository redisTokenRepository;
    private final TokenUsageLimiter tokenUsageLimiter;
    private final AiAgentClient aiAgentClient;
    private final Locker locker;

    @Override
    public AgentResult execute(long userId, String message) {
        if (StringUtils.isBlank(message)) {
            return new AgentResult("Please ask a question");
        }

        UUID lockUuid = UUID.randomUUID();
        if (locker.lock(String.valueOf(userId), lockUuid)) {
            try {
                return retrieveAgentReply(userId, message);
            } finally {
                locker.unlock(String.valueOf(userId), lockUuid);
            }
        }
        throw new TokenLockException("Failed to acquire agent lock");
    }

    private @NonNull AgentResult retrieveAgentReply(long userId, String message) {
        long tokenUsage = calculateTokenUsage(message);
        validateTokenLimit(userId, tokenUsage);
        String agentResponse = aiAgentClient.ask(message);
        redisTokenRepository.incrementTokenUsage(tokenUsage, userId);
        imitationLongProcess();
        return new AgentResult(agentResponse);
    }

    private void imitationLongProcess() {
        if (isUseImitationProcess) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void validateTokenLimit(long userId, long tokenUsage) {
        log.info("Checking token usage for user: {} with token usage: {}", userId, tokenUsage);
        if (tokenUsageLimiter.isTokenLimitExceeded(userId, tokenUsage)) {
            throw new TokenLimitExceeded("Token usage limit exceeded");
        }
    }

    private long calculateTokenUsage(String message) {
        return (long) Math.ceil(message.length() / 50.0) * 10;
    }
}
