package com.terstredisproject1.infrastructure.db.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terstredisproject1.domain.model.agent.AgentDelayedMessage;
import com.terstredisproject1.domain.model.agent.DelayedMessage;
import com.terstredisproject1.domain.model.agent.PeriodType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisDelayedMessageRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DELAYED_MESSAGE_QUEUE_KEY = "queue:ai:delayed";

    public void saveMessage(long userId, AgentDelayedMessage agentDelayedMessage) {
        String json = toJson(new DelayedMessage(userId, agentDelayedMessage.message()));
        stringRedisTemplate.opsForZSet().add(
                DELAYED_MESSAGE_QUEUE_KEY,
                json,
                calculateScore(agentDelayedMessage.periodType(), agentDelayedMessage.periodValue()));
    }

    public List<DelayedMessage> getReadyMessages() {
        Set<String> messages = stringRedisTemplate.opsForZSet().rangeByScore(
                DELAYED_MESSAGE_QUEUE_KEY,
                0,
                (double) Instant.now().getEpochSecond());

        return messages.stream()
                .map(this::fromJson)
                .toList();
    }

    public void deleteMessage(DelayedMessage message) {
        stringRedisTemplate.opsForZSet().remove(DELAYED_MESSAGE_QUEUE_KEY, toJson(message));
    }

    @SneakyThrows
    private String toJson(DelayedMessage delayedMessage) {
        return objectMapper.writeValueAsString(delayedMessage);
    }

    @SneakyThrows
    private DelayedMessage fromJson(String json) {
        return objectMapper.readValue(json, DelayedMessage.class);
    }

    private double calculateScore(PeriodType periodType, int periodValue) {
        return switch (periodType) {
            case MINUTES -> (double) Instant.now().plus(periodValue, ChronoUnit.MINUTES).getEpochSecond();
            case HOURS -> (double) Instant.now().plus(periodValue, ChronoUnit.HOURS).getEpochSecond();
            case DAYS -> (double) Instant.now().plus(periodValue, ChronoUnit.DAYS).getEpochSecond();
        };
    }
}
