package com.terstredisproject1.infrastructure.db.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terstredisproject1.domain.model.agent.AgentDilayMessage;
import com.terstredisproject1.domain.model.agent.DilayMessage;
import com.terstredisproject1.domain.model.agent.PeriodType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisDilayMessageRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private static final String DELAYED_MESSAGE_QUEUE_KEY = "queue:ai:delayed";


    public void saveMessage(long userId, AgentDilayMessage agentDelayMessage) {
        String json = toJson(new DilayMessage(userId, agentDelayMessage.message()));
        stringRedisTemplate.opsForZSet().add(
                DELAYED_MESSAGE_QUEUE_KEY,
                json,
                calculateDilay(agentDelayMessage.periodType(), agentDelayMessage.periodValue()));
    }

    public List<DilayMessage> getDilayMessageTask() {
        final Set<String> strings = stringRedisTemplate.opsForZSet().rangeByScore(DELAYED_MESSAGE_QUEUE_KEY,
                0,
                longToDuble(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

        return strings.stream()
                .map(this::fromJson)
                .toList();
    }

    public void removeMessage(DilayMessage message) {
        stringRedisTemplate.opsForZSet().remove(DELAYED_MESSAGE_QUEUE_KEY, toJson(message));
    }

    @SneakyThrows
    private String toJson(DilayMessage dilayMessage) {
        return objectMapper.writeValueAsString(dilayMessage);
    }

    @SneakyThrows
    public DilayMessage fromJson(String json) {
        return objectMapper.readValue(json, DilayMessage.class);
    }

    private Double calculateDilay(PeriodType periodType, int periodValue) {
        return switch (periodType) {
            case PeriodType.MINUTES ->
                    longToDuble(LocalDateTime.now().plusMinutes(periodValue).toEpochSecond(ZoneOffset.UTC));
            case PeriodType.HOURS ->
                    longToDuble(LocalDateTime.now().plusHours(periodValue).toEpochSecond(ZoneOffset.UTC));
            case PeriodType.DAYS ->
                    longToDuble(LocalDateTime.now().plusDays(periodValue).toEpochSecond(ZoneOffset.UTC));
        };
    }

    private Double longToDuble(Long value) {
        return value.doubleValue();
    }
}
