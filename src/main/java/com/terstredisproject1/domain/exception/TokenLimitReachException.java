package com.terstredisproject1.domain.exception;

public class TokenLimitReachException extends RuntimeException {

    public TokenLimitReachException(String message) {
        super(message);
    }
}
