package com.terstredisproject1.infrastructure.locker;

import com.terstredisproject1.infrastructure.db.redis.RedisLockerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLocker implements Locker {
    private final RedisLockerRepository redisLockerRepository;

    @Override
    public boolean lock(String lockerId) {
        return redisLockerRepository.lock(lockerId);
    }

    @Override
    public void unlock(String lockerId) {
        redisLockerRepository.unlock(lockerId);
    }
}
