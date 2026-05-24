package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.AgentDelayedMessage;

public interface SetDelayedMessagePort {

    void scheduleMessage(long userId, AgentDelayedMessage delayedMessage);
}
