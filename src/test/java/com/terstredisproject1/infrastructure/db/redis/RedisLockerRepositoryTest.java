package com.terstredisproject1.infrastructure.db.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisLockerRepositoryTest {

    private static final String LOCKER_ID = "42";
    private static final String EXPECTED_KEY = "locker:42";

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisLockerRepository repository;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void lock_returns_true_when_redis_key_set_successfully() {
        UUID uuid = UUID.randomUUID();
        when(valueOperations.setIfAbsent(eq(EXPECTED_KEY), eq(uuid.toString()), any(Duration.class)))
                .thenReturn(true);

        assertTrue(repository.lock(LOCKER_ID, uuid));
    }

    @Test
    void lock_returns_false_when_key_already_exists() {
        UUID uuid = UUID.randomUUID();
        when(valueOperations.setIfAbsent(eq(EXPECTED_KEY), eq(uuid.toString()), any(Duration.class)))
                .thenReturn(false);

        assertFalse(repository.lock(LOCKER_ID, uuid));
    }

    @Test
    void lock_returns_false_when_redis_returns_null() {
        UUID uuid = UUID.randomUUID();
        when(valueOperations.setIfAbsent(eq(EXPECTED_KEY), eq(uuid.toString()), any(Duration.class)))
                .thenReturn(null);

        assertFalse(repository.lock(LOCKER_ID, uuid));
    }

    @Test
    void unlock_deletes_key_when_uuid_matches() {
        UUID uuid = UUID.randomUUID();
        when(valueOperations.get(EXPECTED_KEY)).thenReturn(uuid.toString());

        repository.unlock(LOCKER_ID, uuid);

        verify(stringRedisTemplate).delete(EXPECTED_KEY);
    }

    @Test
    void unlock_does_not_delete_when_uuid_does_not_match() {
        UUID ownerUuid = UUID.randomUUID();
        UUID callerUuid = UUID.randomUUID();
        when(valueOperations.get(EXPECTED_KEY)).thenReturn(ownerUuid.toString());

        repository.unlock(LOCKER_ID, callerUuid);

        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    void unlock_does_not_delete_when_key_has_already_expired() {
        UUID uuid = UUID.randomUUID();
        when(valueOperations.get(EXPECTED_KEY)).thenReturn(null);

        repository.unlock(LOCKER_ID, uuid);

        verify(stringRedisTemplate, never()).delete(anyString());
    }

    private static String anyString() {
        return any(String.class);
    }
}