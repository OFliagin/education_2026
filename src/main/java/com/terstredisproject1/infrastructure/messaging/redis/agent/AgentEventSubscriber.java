package com.terstredisproject1.infrastructure.messaging.redis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AgentEventSubscriber {

    public void onMessage(String message) {
        log.info("Received message: {}", message);
    }
}
