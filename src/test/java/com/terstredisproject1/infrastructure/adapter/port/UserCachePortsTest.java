package com.terstredisproject1.infrastructure.adapter.port;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.infrastructure.db.UserRepository;
import com.terstredisproject1.usecase.user.port.CreateUserPort;
import com.terstredisproject1.usecase.user.port.GetUserPort;
import com.terstredisproject1.usecase.user.port.UpdateUserPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(UserCachePortsTest.TestConfig.class)
class UserCachePortsTest {

    private static final String USERS_CACHE = "users";

    @jakarta.annotation.Resource
    private CreateUserPort createUserPort;

    @jakarta.annotation.Resource
    private GetUserPort getUserPort;

    @jakarta.annotation.Resource
    private UpdateUserPort updateUserPort;

    @jakarta.annotation.Resource
    private UserRepository userRepository;

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @AfterEach
    void tearDown() {
        clearInvocations(userRepository);
        Cache usersCache = cacheManager.getCache(USERS_CACHE);
        if (usersCache != null) {
            usersCache.clear();
        }
    }

    @Test
    void create_user_populates_cache_immediately() {
        User newUser = User.builder()
                .username("alice")
                .email("alice@example.com")
                .build();
        User savedUser = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .build();
        when(userRepository.save(newUser)).thenReturn(savedUser);

        User createdUser = createUserPort.createUser(newUser);

        assertEquals(1L, createdUser.getId());
        User cachedUser = getRequiredCache().get(1L, User.class);
        assertNotNull(cachedUser);
        assertEquals("alice", cachedUser.getUsername());
        assertEquals("alice@example.com", cachedUser.getEmail());
    }

    @Test
    void get_user_uses_cache_after_first_load() {
        User storedUser = User.builder()
                .id(2L)
                .username("bob")
                .email("bob@example.com")
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(storedUser));

        User firstRead = getUserPort.getUser(2L);
        User secondRead = getUserPort.getUser(2L);

        assertEquals("bob", firstRead.getUsername());
        assertEquals("bob", secondRead.getUsername());
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void update_user_refreshes_existing_cache_entry() {
        User storedUser = User.builder()
                .id(3L)
                .username("charlie")
                .email("charlie@example.com")
                .build();
        User updateRequest = User.builder()
                .username("charlie-updated")
                .email("charlie.updated@example.com")
                .build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(storedUser));
        when(userRepository.save(storedUser)).thenReturn(storedUser);
        getRequiredCache().put(3L, User.builder()
                .id(3L)
                .username("stale")
                .email("stale@example.com")
                .build());

        User updatedUser = updateUserPort.updateUser(3L, updateRequest);

        assertEquals("charlie-updated", updatedUser.getUsername());
        assertEquals("charlie.updated@example.com", updatedUser.getEmail());
        User cachedUser = getRequiredCache().get(3L, User.class);
        assertNotNull(cachedUser);
        assertEquals("charlie-updated", cachedUser.getUsername());
        assertEquals("charlie.updated@example.com", cachedUser.getEmail());
    }

    private Cache getRequiredCache() {
        Cache usersCache = cacheManager.getCache(USERS_CACHE);
        assertNotNull(usersCache);
        return usersCache;
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        CreateUserPortImpl createUserPort(UserRepository userRepository) {
            return new CreateUserPortImpl(userRepository);
        }

        @Bean
        GetUserPortImpl getUserPort(UserRepository userRepository) {
            return new GetUserPortImpl(userRepository);
        }

        @Bean
        UpdateUserPortImpl updateUserPort(UserRepository userRepository) {
            return new UpdateUserPortImpl(userRepository);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(USERS_CACHE);
        }
    }
}
