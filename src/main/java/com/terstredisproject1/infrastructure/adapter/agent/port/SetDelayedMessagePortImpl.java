package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.agent.AgentDelayedMessage;
import com.terstredisproject1.infrastructure.db.redis.RedisDelayedMessageRepository;
import com.terstredisproject1.usecase.agent.port.SetDelayedMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetDelayedMessagePortImpl implements SetDelayedMessagePort {

    private final RedisDelayedMessageRepository redisDelayedMessageRepository;

    @Override
    public void scheduleMessage(long userId, AgentDelayedMessage delayedMessage) {
        redisDelayedMessageRepository.saveMessage(userId, delayedMessage);
    }
}
