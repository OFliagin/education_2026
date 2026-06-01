package com.terstredisproject1.infrastructure.streaming.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AgentEventConsumer {
    private static final String CONSUMER_GROUP_NAME = "ai-task-event-processors";
    private static final String CONSUMER_NAME = "app-instance-1";
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${ai.task.events.stream.key:ai:task:stream:events}")
    private String streamKey;


    @PostConstruct
    public void init() {
        try {
            stringRedisTemplate.opsForStream().createGroup(
                    streamKey,
                    ReadOffset.from("0"),
                    CONSUMER_GROUP_NAME);
        } catch (Exception e) {
            log.warn("Stream group '{}' already exists or stream not yet created, will retry on first message: {}", CONSUMER_GROUP_NAME, e.getMessage());
        }
    }


    @Scheduled(fixedRate = 1000)
    public void consume() {
        try {
            final List<MapRecord<String, Object, Object>> messages = getRecords();
            if (!CollectionUtils.isEmpty(messages)) {
                processMessage(messages);
            }
        } catch (Exception e) {
            log.error("Failed to read from stream '{}': {}", streamKey, e.getMessage());
        }
    }

    private void processMessage(List<MapRecord<String, Object, Object>> records) {
        for (MapRecord<String, Object, Object> entry : records) {
            try {
                log.info("Received stream event. id={}, body={}", entry.getId(), entry.getValue());
                stringRedisTemplate.opsForStream().acknowledge(
                        streamKey,
                        CONSUMER_GROUP_NAME,
                        entry.getId()
                );
                log.info("Stream event acknowledged. id={}", entry.getId());
            } catch (Exception e) {
                log.error("Failed to process stream event. id={}", entry.getId(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<MapRecord<String, Object, Object>> getRecords() {
        return stringRedisTemplate.opsForStream().read(
                Consumer.from(CONSUMER_GROUP_NAME, CONSUMER_NAME),
                StreamReadOptions.empty()
                        .count(10)
                        .block(Duration.ofMillis(900)),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed())
        );
    }
}
