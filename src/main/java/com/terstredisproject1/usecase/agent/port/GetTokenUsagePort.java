package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.TokenUsage;

public interface GetTokenUsagePort {

    TokenUsage get(long userId);
}
