package com.terstredisproject1.domain.model.agent;

public record AgentDelayedMessage(String message, PeriodType periodType, int periodValue) {
}
