package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.agent.DilayMessage;
import com.terstredisproject1.infrastructure.db.redis.RedisDilayMessageRepository;
import com.terstredisproject1.usecase.agent.port.DeleteDilayMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteDilayMessagePortImpl implements DeleteDilayMessagePort {
    private final RedisDilayMessageRepository redisDilayMessageRepository;

    @Override
    public void getDilayMessageTask(DilayMessage dilayMessage) {
        redisDilayMessageRepository.removeMessage(dilayMessage);
    }
}
