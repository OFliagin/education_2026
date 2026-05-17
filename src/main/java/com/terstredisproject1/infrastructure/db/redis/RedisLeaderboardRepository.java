package com.terstredisproject1.infrastructure.db.redis;

import com.terstredisproject1.domain.model.leaderboard.LeaderboardUserInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class RedisLeaderboardRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String LEADERBOARD_KEY = "leaderboard:global";

    public long incrementScore(long userId) {
        final Double scoreAfterIncrement = stringRedisTemplate.opsForZSet()
                                                    .incrementScore(LEADERBOARD_KEY, String.valueOf(userId), 10);
        return scoreAfterIncrement.longValue();
    }


    public long getScore(long userId) {
        return Optional.ofNullable(stringRedisTemplate.opsForZSet().score(LEADERBOARD_KEY, String.valueOf(userId)))
                .map(Double::longValue).orElse(0L);
    }

    public int getUserPosition(long userId) {
        return Optional.ofNullable(stringRedisTemplate.opsForZSet().rank(LEADERBOARD_KEY, String.valueOf(userId)))
                .map(Long::intValue).orElse(0);
    }

    public List<LeaderboardUserInfo> getTop(int limit, int offset) {
        final Set<ZSetOperations.@NonNull TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                                                            .reverseRangeWithScores(LEADERBOARD_KEY, offset, (limit - 1));
        List<LeaderboardUserInfo> topUsers = new ArrayList<>();
        int rank = 1;
        if (typedTuples != null) {
            for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
                String userId = tuple.getValue();
                Double score = tuple.getScore();
                if (StringUtils.isBlank(userId) || score == null) {
                    continue;
                }
                topUsers.add(LeaderboardUserInfo.builder()
                        .rank(rank++)
                        .userId(Long.parseLong(userId))
                        .score(score.intValue())
                        .build());
            }
        }
        return topUsers;
    }
}
