package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.DelayedMessage;
import com.terstredisproject1.usecase.agent.port.ProcessDelayedMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetDelayedMessageUseCase {

    private final ProcessDelayedMessagePort processDelayedMessagePort;

    public List<DelayedMessage> execute() {
        return processDelayedMessagePort.getReadyMessages();
    }
}
