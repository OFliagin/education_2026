package com.terstredisproject1.domain.model;

import lombok.Getter;

@Getter
public enum PaymentPlan {
    BASIC(100),
    PRO(2000),
    PREMIUM(5000);

    private final long availableTokens;


    private PaymentPlan(long availableTokens) {
        this.availableTokens = availableTokens;
    }

}
