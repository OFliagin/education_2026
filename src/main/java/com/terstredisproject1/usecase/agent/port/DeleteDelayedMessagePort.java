package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.DelayedMessage;

public interface DeleteDelayedMessagePort {

    void delete(DelayedMessage delayedMessage);
}
