package com.terstredisproject1.adapter.controller.api.agent.request;

import jakarta.validation.constraints.NotNull;

public record AgentRequest(@NotNull Long userId, @NotNull String message) {
}
