package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.agent.DilayMessage;
import com.terstredisproject1.infrastructure.db.redis.RedisDilayMessageRepository;
import com.terstredisproject1.usecase.agent.port.ProcessDilayMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcessDilayMessagePortImpl implements ProcessDilayMessagePort {
    private final RedisDilayMessageRepository redisDilayMessageRepository;

    @Override
    public List<DilayMessage> getDilayMessageTask() {
        return redisDilayMessageRepository.getDilayMessageTask();
    }
}
