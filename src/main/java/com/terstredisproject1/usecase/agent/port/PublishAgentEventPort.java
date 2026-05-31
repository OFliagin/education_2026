package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.AgentMessageEvent;

public interface PublishAgentEventPort {

    void publishTaskCompleted(AgentMessageEvent event);
}
