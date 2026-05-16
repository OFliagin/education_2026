package com.terstredisproject1.domain.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRateLimitState {
    private long userId;
    private int availableTokens;
    private long resetLimitEpochMillis;
}
