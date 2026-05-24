package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.AgentDilayMessage;
import com.terstredisproject1.usecase.agent.port.SetDilayMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetDilayMessageUseCase {
    private final SetDilayMessagePort setDilayMessagePort;

    public void setMessage(long userId, AgentDilayMessage dilayMessage) {
        log.info("Setting delayed message for user: {}, message: {}, periodType: {}, periodValue: {}", userId, dilayMessage.message(), dilayMessage.periodType(), dilayMessage.periodValue());
        setDilayMessagePort.setMessage(userId, dilayMessage);
    }
}
