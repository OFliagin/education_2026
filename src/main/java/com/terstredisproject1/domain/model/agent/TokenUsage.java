package com.terstredisproject1.domain.model.agent;

public record TokenUsage(long totalTokens,
                         long usedTokens,
                         int usagePercent,
                         long remainingTokens,
                         boolean limitExceeded) {
}
