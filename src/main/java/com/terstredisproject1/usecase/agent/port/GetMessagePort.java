package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.AgentResult;

public interface GetMessagePort {

    AgentResult execute(long userId, String message);
}
