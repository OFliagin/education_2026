package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.DelayedMessage;
import com.terstredisproject1.usecase.agent.port.DeleteDelayedMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteDelayedMessageUseCase {

    private final DeleteDelayedMessagePort deleteDelayedMessagePort;

    public void delete(DelayedMessage delayedMessage) {
        deleteDelayedMessagePort.delete(delayedMessage);
    }
}
