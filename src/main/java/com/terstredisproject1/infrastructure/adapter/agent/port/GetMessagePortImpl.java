package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.exception.TokenLimitReachException;
import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.infrastructure.adapter.agent.TokenUsageLimiter;
import com.terstredisproject1.infrastructure.db.redis.RedisTokenRepository;
import com.terstredisproject1.usecase.agent.port.GetMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetMessagePortImpl implements GetMessagePort {
    private final RedisTokenRepository redisTokenRepository;
    private final TokenUsageLimiter tokenUsageLimiter;

    @Override
    public AgentResult execute(long userId, String message) {
        if (StringUtils.isBlank(message)) {
            return new AgentResult("Please ask a question");
        }

        updateTokenUsage(userId, message);

        return new AgentResult("Great question:" + message);
    }

    private void updateTokenUsage(long userId, String message) {
        log.info("Updating token usage for user: {} with message length: {}", userId, message.length());
        long tokenUsage = calculateTokenUsage(message);
        if (tokenUsageLimiter.isLimitRich(userId, tokenUsage)) {
            throw new TokenLimitReachException("Token usage limit exceeded");
        }
        redisTokenRepository.incrementTokenUsage(tokenUsage, userId);
    }

    private long calculateTokenUsage(String message) {
        return (long) Math.ceil(message.length() / 50.0) * 10;
    }
}
