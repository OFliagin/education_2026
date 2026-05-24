package com.terstredisproject1.domain.exception;

public class PaymentProfileNotActiveException extends RuntimeException {

    public PaymentProfileNotActiveException(String message) {
        super(message);
    }
}