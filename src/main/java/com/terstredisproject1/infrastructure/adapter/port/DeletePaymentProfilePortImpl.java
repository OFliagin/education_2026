package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.infrastructure.db.redis.RedisPaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.DeletePaymentProfilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeletePaymentProfilePortImpl implements DeletePaymentProfilePort {
    private final RedisPaymentProfileRepository redisPaymentProfileRepository;

    @Override
    public void execute(long userId) {
        redisPaymentProfileRepository.delete(userId);
        log.info("Payment profile deleted for user: {}", userId);
    }
}
