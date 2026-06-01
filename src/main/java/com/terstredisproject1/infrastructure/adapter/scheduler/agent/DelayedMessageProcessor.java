package com.terstredisproject1.infrastructure.adapter.scheduler.agent;

import com.terstredisproject1.domain.model.agent.AgentMessageEvent;
import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.domain.model.agent.DelayedMessage;
import com.terstredisproject1.usecase.agent.DeleteDelayedMessageUseCase;
import com.terstredisproject1.usecase.agent.GetDelayedMessageUseCase;
import com.terstredisproject1.usecase.agent.GetMessageUseCase;
import com.terstredisproject1.usecase.agent.port.PublishAgentEventPort;
import com.terstredisproject1.usecase.agent.port.StreamAgentEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelayedMessageProcessor {

    private final GetDelayedMessageUseCase getDelayedMessageUseCase;
    private final GetMessageUseCase getMessageUseCase;
    private final DeleteDelayedMessageUseCase deleteDelayedMessageUseCase;
    private final PublishAgentEventPort publishAgentEventPort;
    private final StreamAgentEventPort streamAgentEventPort;

    @Value("${ai.task.event.stream.enabled:true}")
    private boolean useStream;

    @Scheduled(fixedDelay = 1000)
    public void process() {
        log.info("Processing delayed messages");
        final List<DelayedMessage> delayedMessages = getDelayedMessageUseCase.execute();
        for (DelayedMessage dm : delayedMessages) {
            final AgentResult result = getMessageUseCase.execute(dm.userId(), dm.message());
            log.info("Message processed: {}", result.message());
            final AgentMessageEvent agentMessageEvent = AgentMessageEvent.builder()
                    .originalMessage(dm.message())
                    .userId(dm.userId())
                    .agentResponse(result.message())
                    .completedAt(Instant.now())
                    .build();

            if (useStream) {
                streamAgentEventPort.appendEvent(agentMessageEvent);
            } else {
                publishAgentEventPort.publishTaskCompleted(agentMessageEvent);
            }
            deleteDelayedMessageUseCase.delete(dm);
        }
    }
}
