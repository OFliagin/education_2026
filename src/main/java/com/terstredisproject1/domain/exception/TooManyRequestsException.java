package com.terstredisproject1.domain.exception;

public class TooManyRequestsException extends RuntimeException{
    public TooManyRequestsException(String message) {
        super(message);
    }
}
