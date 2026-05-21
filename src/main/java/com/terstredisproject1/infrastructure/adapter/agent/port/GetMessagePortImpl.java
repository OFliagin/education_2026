package com.terstredisproject1.infrastructure.adapter.agent.port;


import com.terstredisproject1.domain.model.agent.AgentResult;
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


    @Override
    public AgentResult execute(long userId, String message) {
        if (StringUtils.isBlank(message)) {
            return AgentResult.builder()
                    .message("Please ask a question")
                    .build();
        }

        updateTokenUsage(userId, message);

        return AgentResult.builder()
                .message("Great question:" + message)
                .build();
    }

    private void updateTokenUsage(long userId, String message) {
        log.info("Updating token usage for user: {} with message length: {}", userId, message.length());
        long tokenUsage = calculateTokenUsage(message);
        redisTokenRepository.incrementTokenUsage(tokenUsage, userId);
    }

    private long calculateTokenUsage(String message) {
        return (long) Math.ceil(message.length() / 50.0) * 10;
    }

}
