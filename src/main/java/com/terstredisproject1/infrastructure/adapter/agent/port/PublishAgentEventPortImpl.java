package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.AgentMessageEvent;
import com.terstredisproject1.usecase.agent.port.PublishAgentEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class PublishAgentEventPortImpl implements PublishAgentEventPort {
    @Value("${ai.task.completed.channel:ai:task:completed}")
    private String taskCompletionChannel;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishTaskCompleted(AgentMessageEvent event) {
        final String eventJson = objectMapper.writeValueAsString(event);
        stringRedisTemplate.convertAndSend(taskCompletionChannel, eventJson);
        log.info("Task completed event published: {}", eventJson);
    }
}
