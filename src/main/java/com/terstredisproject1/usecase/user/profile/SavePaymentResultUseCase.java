package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.adapter.controller.request.SavePaymentResultRequest;
import com.terstredisproject1.domain.model.PaymentProcessStatus;
import com.terstredisproject1.usecase.user.port.SavePaymentResultPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SavePaymentResultUseCase {
    private final SavePaymentResultPort savePaymentResultPort;


    public void savePaymentResult(long userId,SavePaymentResultRequest savePaymentResultRequest) {
        final PaymentProcessStatus status = PaymentProcessStatus.valueOf(savePaymentResultRequest.status());

        log.info("Saving payment result for user: {} with status: {}", userId, status);
        savePaymentResultPort.savePaymentResult(userId,
                savePaymentResultRequest.amountInCents(),
                status);
    }
}
