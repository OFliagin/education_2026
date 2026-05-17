package com.terstredisproject1.adapter.controller.api.agent;

import com.terstredisproject1.adapter.controller.api.agent.request.AgentRequest;
import com.terstredisproject1.adapter.controller.api.agent.response.AgentResponse;
import com.terstredisproject1.usecase.agent.GetMessageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgentController {
    private final GetMessageUseCase getMessageUseCase;

    @PostMapping("/api/agent/message")
    public AgentResponse getAgentResponse(@Valid @RequestBody AgentRequest request) {
        return getMessageUseCase.execute(request);
    }
}
