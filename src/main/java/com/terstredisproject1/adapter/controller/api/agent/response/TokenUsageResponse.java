package com.terstredisproject1.adapter.controller.api.agent.response;

public record TokenUsageResponse(long totalTokens,
                                 long usedTokens,
                                 int usagePercent,
                                 long remainingTokens,
                                 boolean limitExceeded) {
}
