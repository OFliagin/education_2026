package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.UserPaymentProfile;
import com.terstredisproject1.infrastructure.db.redis.RedisPaymentProfileRepository;
import com.terstredisproject1.usecase.user.port.GetPaymentProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetPaymentProfilePortImpl implements GetPaymentProfilePort {
    private final RedisPaymentProfileRepository redisPaymentProfileRepository;

    @Override
    public Optional<UserPaymentProfile> execute(long userId) {
        return Optional.ofNullable(redisPaymentProfileRepository.findByUserId(userId));
    }
}
