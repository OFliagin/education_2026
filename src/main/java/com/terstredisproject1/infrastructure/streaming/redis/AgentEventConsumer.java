package com.terstredisproject1.infrastructure.streaming.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AgentEventConsumer {
    private static final String CONSUMER_GROUP_NAME = "ai-task-event-processors";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${ai.task.events.stream.key:ai:task:stream:events}")
    private String streamKey;
    @Value("${ai.task.events.stream.consumer.name:app-instance}-${random.uuid}")
    private String consumerName1;
    @Value("${ai.task.events.stream.consumer.name:app-instance}-${random.uuid}")
    private String consumerName2;
    @Value("${ai.task.events.stream.consumer.name:app-instance-recovery}")
    private String recoveryConsumerName;


    @PostConstruct
    public void init() {
        try {
            stringRedisTemplate.opsForStream().add(
                    streamKey,
                    Map.of("init", "true")
            );

            stringRedisTemplate.opsForStream().createGroup(
                    streamKey,
                    ReadOffset.from("0"),
                    CONSUMER_GROUP_NAME);
        } catch (Exception e) {
            log.warn("Stream group '{}' already exists or stream not yet created, will retry on first message: {}", CONSUMER_GROUP_NAME, e.getMessage());
        }
    }


    @Scheduled(fixedRate = 1000)
    public void consume1() {
        try {
            final List<MapRecord<String, Object, Object>> messages = getRecords(consumerName1);
            if (!CollectionUtils.isEmpty(messages)) {
                processMessage(messages);
            }
        } catch (Exception e) {
            log.error("Failed to read from stream {}", streamKey, e);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void recovery() {
        try {
            final List<MapRecord<String, Object, Object>> messages = getRecords(recoveryConsumerName, true);
            if (!CollectionUtils.isEmpty(messages)) {
                log.info("recovery messages: {}", messages);
                processMessage(messages);
            }
        } catch (Exception e) {
            log.error("Failed to read from stream {}", streamKey, e);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void consume2() {
        try {
            final List<MapRecord<String, Object, Object>> messages = getRecords(consumerName2);
            if (!CollectionUtils.isEmpty(messages)) {
                processMessage(messages);
            }
        } catch (Exception e) {
            log.error("Failed to read from stream '{}': {}", streamKey, e.getMessage());
        }
    }


    @Scheduled(fixedRate = 10000)
    public void recoverPendingMessages() {
        try {
            claimExpiredPendingMessages();
        } catch (Exception e) {
            log.error("Failed to read from stream '{}': {}", streamKey, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void claimExpiredPendingMessages() {
        PendingMessages pending = stringRedisTemplate.opsForStream().pending(
                streamKey,
                CONSUMER_GROUP_NAME,
                Range.unbounded(),
                10L
        );
        if (pending.isEmpty()) return;

        List<RecordId> idleIds = pending.stream()
                .filter(msg -> msg.getElapsedTimeSinceLastDelivery().toSeconds() >= 60)
                .map(PendingMessage::getId)
                .toList();
        if (idleIds.isEmpty()) return;

        log.info("Reclaiming {} pending messages idle for >=60s — handing off to recovery consumer", idleIds.size());
        stringRedisTemplate.opsForStream().claim(
                streamKey,
                CONSUMER_GROUP_NAME,
                recoveryConsumerName,
                Duration.ofSeconds(60),
                idleIds.toArray(RecordId[]::new)
        );
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

    private List<MapRecord<String, Object, Object>> getRecords(String consumerName, boolean fromFirst) {
        final List<@NonNull MapRecord<String, Object, Object>> read = stringRedisTemplate.opsForStream().read(
                Consumer.from(CONSUMER_GROUP_NAME, consumerName),
                StreamReadOptions.empty()
                        .count(10)
                        .block(Duration.ofMillis(900)),
                StreamOffset.create(streamKey, fromFirst ? ReadOffset.from("0") : ReadOffset.lastConsumed())
        );
        if (!CollectionUtils.isEmpty(read) && consumerName.equals(consumerName1)) {
            throw new RuntimeException("broking consumer for test");
        }
        return read;
    }


    private List<MapRecord<String, Object, Object>> getRecords(String consumerName) {
        return getRecords(consumerName, false);
    }
}
