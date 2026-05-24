package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.DelayedMessage;

import java.util.List;

public interface ProcessDelayedMessagePort {

    List<DelayedMessage> getReadyMessages();
}
