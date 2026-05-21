package com.terstredisproject1.infrastructure.adapter.agent.port;


import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.infrastructure.db.redis.RedisTokenRepository;
import com.terstredisproject1.usecase.agent.port.GetMessagePort;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
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
        long tokenUsage = calculateTokenUsage(message);
        redisTokenRepository.incrementTokenUsage(userId, tokenUsage);
    }

    private long calculateTokenUsage(String message) {
        return (long) Math.ceil(message.length() / 50.0) * 10;
    }

}
