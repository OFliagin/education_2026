package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.AgentMessageEvent;

public interface PublishAgentEventPort {

    void publishTaskCompleted(AgentMessageEvent event);
}
