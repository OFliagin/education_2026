package com.terstredisproject1.usecase.leaderboard;

import com.terstredisproject1.domain.model.User;
import com.terstredisproject1.domain.model.leaderboard.LeaderboardUserInfo;
import com.terstredisproject1.usecase.leaderboard.port.GetLeaderboardPort;
import com.terstredisproject1.usecase.user.port.GetUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetLeaderboardUseCase {
    private final GetLeaderboardPort getLeaderboardPort;
    private final GetUserPort getUserPort;

    public List<LeaderboardUserInfo> getTopUsers(int limit, int offset) {
        log.info("Getting top users with limit: {} and offset: {}", limit, offset);
        return getLeaderboardPort.getTopUsers(limit, offset).stream()
                .peek(this::setUserName)
                .toList();
    }

    private void setUserName(LeaderboardUserInfo info) {
        final User user1 = getUserPort.getUser(info.getUserId());
        info.setUsername(user1.getUsername());
    }

    public Long getRank(long userId) {
        log.info("Getting rank for user with id: {}", userId);
        return getLeaderboardPort.getRank(userId);
    }

    public long getScore(long userId) {
        log.info("Getting score for user with id: {}", userId);
        return getLeaderboardPort.getScore(userId);
    }

    public long incrementScore(long userId) {
        log.info("Incrementing score for user with id: {}", userId);
        return getLeaderboardPort.incrementScore(userId);
    }
}
