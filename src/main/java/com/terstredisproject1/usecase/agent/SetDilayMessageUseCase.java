package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.AgentDilayMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetDilayMessageUseCase {

    public void setMessage(long userId, AgentDilayMessage d) {
        log.info("Setting delayed message for user: {}, message: {}, periodType: {}, periodValue: {}", userId, d.message(), d.periodType(), d.periodValue());
    }
}
