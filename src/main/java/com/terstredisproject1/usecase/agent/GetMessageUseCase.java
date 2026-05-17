package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.adapter.controller.api.agent.request.AgentRequest;
import com.terstredisproject1.adapter.controller.api.agent.response.AgentResponse;
import com.terstredisproject1.usecase.agent.port.GetMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMessageUseCase {
    private final GetMessagePort getMessagePort;

    public AgentResponse execute(AgentRequest request) {
        return new AgentResponse(request.userId(), request.message(),
                getMessagePort.execute(request.userId(),
                        request.message()).getMessage());
    }
}
