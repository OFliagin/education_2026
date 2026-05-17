package com.terstredisproject1.infrastructure.adapter.agent.port;


import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.usecase.agent.port.GetMessagePort;
import org.springframework.stereotype.Component;

@Component
public class GetMessagePortImpl implements GetMessagePort {
    @Override
    public AgentResult execute(long userId, String message) {
        return AgentResult.builder()
                .message("Great question:" + message)
                .build();
    }
}
