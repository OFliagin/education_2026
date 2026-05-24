package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.AgentDelayedMessage;
import com.terstredisproject1.usecase.agent.port.SetDelayedMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetDelayedMessageUseCase {

    private final SetDelayedMessagePort setDelayedMessagePort;

    public void scheduleMessage(long userId, AgentDelayedMessage delayedMessage) {
        log.info("Scheduling delayed message for user: {}, message: {}, periodType: {}, periodValue: {}", userId, delayedMessage.message(), delayedMessage.periodType(), delayedMessage.periodValue());
        setDelayedMessagePort.scheduleMessage(userId, delayedMessage);
    }
}
