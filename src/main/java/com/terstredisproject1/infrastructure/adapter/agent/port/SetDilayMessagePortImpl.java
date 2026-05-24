package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.agent.AgentDilayMessage;
import com.terstredisproject1.infrastructure.db.redis.RedisDilayMessageRepository;
import com.terstredisproject1.usecase.agent.port.SetDilayMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetDilayMessagePortImpl implements SetDilayMessagePort {
    private final RedisDilayMessageRepository redisDilayMessageRepository;

    @Override
    public void setMessage(long userId, AgentDilayMessage dilayMessage) {
        redisDilayMessageRepository.saveMessage(userId, dilayMessage);
    }
}
