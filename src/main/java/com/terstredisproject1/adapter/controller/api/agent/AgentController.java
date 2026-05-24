package com.terstredisproject1.adapter.controller.api.agent;

import com.terstredisproject1.adapter.controller.api.agent.request.AgentDelayedMessageRequest;
import com.terstredisproject1.adapter.controller.api.agent.request.AgentRequest;
import com.terstredisproject1.adapter.controller.api.agent.response.AgentResponse;
import com.terstredisproject1.adapter.controller.api.agent.response.TokenUsageResponse;
import com.terstredisproject1.domain.exception.TokenLimitExceeded;
import com.terstredisproject1.domain.exception.TokenLockException;
import com.terstredisproject1.domain.model.agent.AgentDelayedMessage;
import com.terstredisproject1.domain.model.agent.PeriodType;
import com.terstredisproject1.domain.model.agent.TokenUsage;
import com.terstredisproject1.usecase.agent.GetMessageUseCase;
import com.terstredisproject1.usecase.agent.GetTokenUsageUseCase;
import com.terstredisproject1.usecase.agent.SetDelayedMessageUseCase;
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
    private final SetDelayedMessageUseCase setDelayedMessageUseCase;

    @PostMapping("/api/agent/message")
    public AgentResponse getAgentResponse(@Valid @RequestBody AgentRequest request) {
        final var result = getMessageUseCase.execute(request.userId(), request.message());
        return new AgentResponse(request.userId(), request.message(), result.message());
    }

    @PostMapping("/api/agent/message/delayed")
    public ResponseEntity<String> delayMessage(@RequestBody AgentDelayedMessageRequest request) {
        setDelayedMessageUseCase.scheduleMessage(request.userId(),
                new AgentDelayedMessage(request.message(),
                        PeriodType.valueOf(request.periodType()),
                        request.periodValue()));
        return ResponseEntity.ok("Message delayed successfully");
    }

    @GetMapping("/api/agent/user/{userId}/token-usage")
    public TokenUsageResponse getTokenUsage(@PathVariable long userId) {
        final TokenUsage tokenUsage = getTokenUsageUseCase.execute(userId);
        return new TokenUsageResponse(tokenUsage.totalTokens(), tokenUsage.usedTokens(), tokenUsage.usagePercent(), tokenUsage.remainingTokens(), tokenUsage.limitExceeded());
    }


    @ExceptionHandler(TokenLimitExceeded.class)
    public ResponseEntity<String> handleTokenLimitExceeded(TokenLimitExceeded ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    @ExceptionHandler(TokenLockException.class)
    public ResponseEntity<String> handleTokenLockException(TokenLockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
