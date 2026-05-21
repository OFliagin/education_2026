package com.terstredisproject1.adapter.controller.api.agent;

import com.terstredisproject1.adapter.controller.api.agent.request.AgentRequest;
import com.terstredisproject1.adapter.controller.api.agent.response.AgentResponse;
import com.terstredisproject1.adapter.controller.api.agent.response.TokenUsageResponse;
import com.terstredisproject1.domain.model.agent.TokenUsage;
import com.terstredisproject1.usecase.agent.GetMessageUseCase;
import com.terstredisproject1.usecase.agent.GetTokenUsageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgentController {
    private final GetMessageUseCase getMessageUseCase;
    private final GetTokenUsageUseCase getTokenUsageUseCase;

    @PostMapping("/api/agent/message")
    public AgentResponse getAgentResponse(@Valid @RequestBody AgentRequest request) {
        return getMessageUseCase.execute(request);
    }

    @GetMapping("/api/agent/user/{userId}/token-usage")
    public TokenUsageResponse getTokenUsage(@PathVariable long userId) {
        final TokenUsage tokenUsage = getTokenUsageUseCase.execute(userId);
        return new TokenUsageResponse(tokenUsage.totalTokens(), tokenUsage.usedTokens());
    }
}
