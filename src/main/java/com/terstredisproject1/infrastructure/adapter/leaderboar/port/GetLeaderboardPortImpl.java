package com.terstredisproject1.infrastructure.adapter.leaderboar.port;

import com.terstredisproject1.domain.model.leaderboard.LeaderboardUserInfo;
import com.terstredisproject1.infrastructure.db.redis.RedisLeaderboardRepository;
import com.terstredisproject1.usecase.leaderboard.port.GetLeaderboardPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetLeaderboardPortImpl implements GetLeaderboardPort {
    private final RedisLeaderboardRepository redisLeaderboardRepository;


    @Override
    public long getRank(long userId) {
        return redisLeaderboardRepository.getUserPosition(userId);
    }

    @Override
    public List<LeaderboardUserInfo> getTopUsers(int limit, int offset) {
        return redisLeaderboardRepository.getTop(limit, offset);
    }

    @Override
    public long getScore(long userId) {
        return redisLeaderboardRepository.getScore(userId);
    }

    @Override
    public long incrementScore(long userId) {
        return redisLeaderboardRepository.incrementScore(userId);
    }
}
