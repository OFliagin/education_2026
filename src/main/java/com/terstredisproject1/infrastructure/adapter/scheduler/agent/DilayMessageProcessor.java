package com.terstredisproject1.infrastructure.adapter.scheduler.agent;

import com.terstredisproject1.domain.model.agent.AgentResult;
import com.terstredisproject1.domain.model.agent.DilayMessage;
import com.terstredisproject1.usecase.agent.DeleteDilayMessageUseCase;
import com.terstredisproject1.usecase.agent.GetDilayMessageUseCase;
import com.terstredisproject1.usecase.agent.GetMessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DilayMessageProcessor {
    private final GetDilayMessageUseCase getDilayMessageUseCase;
    private final GetMessageUseCase getMessageUseCase;
    private final DeleteDilayMessageUseCase deleteDilayMessageUseCase;

    @Scheduled(fixedDelay = 1000)
    public void process() {
        log.info("Processing delayed messages");
        final List<DilayMessage> dilayMessages = getDilayMessageUseCase.execute();
        for (DilayMessage dm : dilayMessages) {
            final AgentResult execute = getMessageUseCase.execute(dm.userId(), dm.message());
            log.info("Message processed: {}", execute.message());
            deleteDilayMessageUseCase.delete(dm);
        }
    }
}
