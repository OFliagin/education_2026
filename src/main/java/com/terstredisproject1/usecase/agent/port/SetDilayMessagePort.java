package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.AgentDilayMessage;

public interface SetDilayMessagePort {

    void setMessage(long userId, AgentDilayMessage dilayMessage);
}
