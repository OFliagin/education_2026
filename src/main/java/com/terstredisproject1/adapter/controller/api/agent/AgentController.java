package com.terstredisproject1.adapter.controller.api.agent;

import com.terstredisproject1.adapter.controller.api.agent.request.AgentRequest;
import com.terstredisproject1.adapter.controller.api.agent.response.AgentResponse;
import com.terstredisproject1.adapter.controller.api.agent.response.TokenUsageResponse;
import com.terstredisproject1.domain.exception.TokenLimitExceeded;
import com.terstredisproject1.domain.model.agent.TokenUsage;
import com.terstredisproject1.usecase.agent.GetMessageUseCase;
import com.terstredisproject1.usecase.agent.GetTokenUsageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AgentController {
    private final GetMessageUseCase getMessageUseCase;
    private final GetTokenUsageUseCase getTokenUsageUseCase;

    @PostMapping("/api/agent/message")
    public AgentResponse getAgentResponse(@Valid @RequestBody AgentRequest request) {
        final var result = getMessageUseCase.execute(request.userId(), request.message());
        return new AgentResponse(request.userId(), request.message(), result.message());

    }

    @GetMapping("/api/agent/user/{userId}/token-usage")
    public TokenUsageResponse getTokenUsage(@PathVariable long userId) {
        final TokenUsage tokenUsage = getTokenUsageUseCase.execute(userId);
        return new TokenUsageResponse(tokenUsage.totalTokens(), tokenUsage.usedTokens(), tokenUsage.usagePercent(), tokenUsage.remainingTokens(), tokenUsage.limitExceeded());
    }


    @ExceptionHandler(TokenLimitExceeded.class)
    public ResponseEntity<String> handleTokenLimitReachException(TokenLimitExceeded ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
