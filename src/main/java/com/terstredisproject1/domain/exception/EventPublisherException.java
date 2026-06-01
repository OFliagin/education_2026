package com.terstredisproject1.domain.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class EventPublisherException extends RuntimeException{
    public EventPublisherException(JsonProcessingException e) {
        super(e);
    }
}
