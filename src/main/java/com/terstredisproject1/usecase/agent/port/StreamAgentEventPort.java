package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.AgentMessageEvent;

public interface StreamAgentEventPort {

    void streaming(AgentMessageEvent event);
}
