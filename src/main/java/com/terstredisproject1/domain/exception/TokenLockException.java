package com.terstredisproject1.domain.exception;

public class TokenLockException extends RuntimeException {

    public TokenLockException(String message) {
        super(message);
    }
}
