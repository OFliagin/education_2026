package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.TokenUsage;
import com.terstredisproject1.usecase.agent.port.GetTokenUsagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTokenUsageUseCase {
    private final GetTokenUsagePort getTokenUsagePort;

    public TokenUsage execute(final long userId) {
        return getTokenUsagePort.get(userId);
    }
}
