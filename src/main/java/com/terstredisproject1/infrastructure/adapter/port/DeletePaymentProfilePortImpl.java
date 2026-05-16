package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.PaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.CreatePaymentProfilePort;
import com.terstredisproject1.usecase.user.port.DeletePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeletePaymentProfilePortImpl implements DeletePaymentProfilePort {
    private final PaymentProfileRepository paymentProfileRepository;

    @Override
    public void execute(long userId) {
        paymentProfileRepository.delete(userId);
        log.info("Payment profile deleted for user: {}", userId);
    }
}
