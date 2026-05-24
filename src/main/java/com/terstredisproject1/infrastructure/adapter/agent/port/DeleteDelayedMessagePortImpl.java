package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.agent.DelayedMessage;
import com.terstredisproject1.infrastructure.db.redis.RedisDelayedMessageRepository;
import com.terstredisproject1.usecase.agent.port.DeleteDelayedMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteDelayedMessagePortImpl implements DeleteDelayedMessagePort {

    private final RedisDelayedMessageRepository redisDelayedMessageRepository;

    @Override
    public void delete(DelayedMessage delayedMessage) {
        redisDelayedMessageRepository.deleteMessage(delayedMessage);
    }
}
