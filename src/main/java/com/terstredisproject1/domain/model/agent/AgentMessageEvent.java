package com.terstredisproject1.domain.model.agent;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Builder
@Value
public class AgentMessageEvent {
    long userId;
    String originalMessage;
    String agentResponse;
    Instant completedAt;
}
