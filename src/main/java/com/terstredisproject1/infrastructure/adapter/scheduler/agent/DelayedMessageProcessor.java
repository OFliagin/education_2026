package com.terstredisproject1.infrastructure.adapter.scheduler.agent;

import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.domain.model.agent.DelayedMessage;
import com.terstredisproject1.usecase.agent.DeleteDelayedMessageUseCase;
import com.terstredisproject1.usecase.agent.GetDelayedMessageUseCase;
import com.terstredisproject1.usecase.agent.GetMessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelayedMessageProcessor {

    private final GetDelayedMessageUseCase getDelayedMessageUseCase;
    private final GetMessageUseCase getMessageUseCase;
    private final DeleteDelayedMessageUseCase deleteDelayedMessageUseCase;

    @Scheduled(fixedDelay = 1000)
    public void process() {
        log.info("Processing delayed messages");
        final List<DelayedMessage> delayedMessages = getDelayedMessageUseCase.execute();
        for (DelayedMessage dm : delayedMessages) {
            final AgentResult result = getMessageUseCase.execute(dm.userId(), dm.message());
            log.info("Message processed: {}", result.message());
            deleteDelayedMessageUseCase.delete(dm);
        }
    }
}
