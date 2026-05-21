package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.usecase.agent.port.GetMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMessageUseCase {
    private final GetMessagePort getMessagePort;

    public AgentResult execute(long userId, String message) {
        return getMessagePort.execute(userId, message);
    }
}
