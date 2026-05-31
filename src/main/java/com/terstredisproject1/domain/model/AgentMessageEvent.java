package com.terstredisproject1.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Builder
@Value
public class AgentMessageEvent {
    long userId;
    String originalMessage;
    String agentResponse;
    LocalDateTime completedAt;
}
