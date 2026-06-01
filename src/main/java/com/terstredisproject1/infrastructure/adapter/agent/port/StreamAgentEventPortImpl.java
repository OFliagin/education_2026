package com.terstredisproject1.infrastructure.adapter.agent.port;

import com.terstredisproject1.domain.model.agent.AgentMessageEvent;
import com.terstredisproject1.usecase.agent.port.StreamAgentEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StreamAgentEventPortImpl implements StreamAgentEventPort {
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${ai.task.events.stream.key:ai:task:stream:events}")
    private String streamKey;

    @Override
    public void streaming(AgentMessageEvent event) {
        stringRedisTemplate.opsForStream().add(
                streamKey,
                Map.of(
                        "userId", String.valueOf(event.getUserId()),
                        "originalMessage", event.getOriginalMessage(),
                        "agentResponse", event.getAgentResponse(),
                        "completedAt", event.getCompletedAt().toString()
                )
        );
    }
}
