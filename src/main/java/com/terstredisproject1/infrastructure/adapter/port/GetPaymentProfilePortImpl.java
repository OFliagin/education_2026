package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.PaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.GetPaymentProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPaymentProfilePortImpl implements GetPaymentProfilePort {
    private final PaymentProfileRepository paymentProfileRepository;

    @Override
    public UserPaymentProfile execute(long userId) {
        return paymentProfileRepository.findByUserId(userId);
    }
}
