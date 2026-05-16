package com.terstredisproject1.usecase.user.profile;

import com.terstredisproject1.usecase.user.port.DeletePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeletePaymentProfileUseCase {
    private final DeletePaymentProfilePort deletePaymentProfilePort;

    public void execute(long userId) {
        deletePaymentProfilePort.execute(userId);
        log.info("Payment profile deleted for user: {}", userId);
    }
}
