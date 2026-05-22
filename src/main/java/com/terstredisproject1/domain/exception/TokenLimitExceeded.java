package com.terstredisproject1.domain.exception;

public class TokenLimitExceeded extends RuntimeException {

    public TokenLimitExceeded(String message) {
        super(message);
    }
}
