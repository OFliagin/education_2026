package com.terstredisproject1.adapter.controller.api.agent.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AgentDilayMessageRequest(@NotNull Long userId, @NotNull String message, @NotNull String periodType, @Min(1) int periodValue) {
}
