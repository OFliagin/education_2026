package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.agent.DelayedMessage;
import com.terstredisproject1.infrastructure.db.redis.RedisDelayedMessageRepository;
import com.terstredisproject1.usecase.agent.port.ProcessDelayedMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcessDelayedMessagePortImpl implements ProcessDelayedMessagePort {

    private final RedisDelayedMessageRepository redisDelayedMessageRepository;

    @Override
    public List<DelayedMessage> getReadyMessages() {
        return redisDelayedMessageRepository.getReadyMessages();
    }
}
