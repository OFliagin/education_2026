package com.terstredisproject1.adapter.controller.request;

public record SavePaymentResultRequest(Long amountInCents, String paymentProcessStatus, String status) {
}
