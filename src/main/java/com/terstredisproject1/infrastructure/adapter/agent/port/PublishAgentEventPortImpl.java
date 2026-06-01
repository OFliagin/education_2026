package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.terstredisproject1.domain.exception.EventPublisherException;
import com.terstredisproject1.domain.model.agent.AgentMessageEvent;
import com.terstredisproject1.usecase.agent.port.PublishAgentEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PublishAgentEventPortImpl implements PublishAgentEventPort {
    @Value("${ai.task.completed.channel:ai:task:completed}")
    private String taskCompletionChannel;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publishTaskCompleted(AgentMessageEvent event) {
        final String eventJson;
        try {
            eventJson = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Error serializing event: {}", event, e);
            throw new EventPublisherException(e);
        }
        stringRedisTemplate.convertAndSend(taskCompletionChannel, eventJson);
        log.info("Task completed event published: {}", eventJson);
    }
}
