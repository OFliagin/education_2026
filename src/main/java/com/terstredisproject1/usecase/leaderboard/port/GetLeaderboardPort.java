package com.terstredisproject1.usecase.leaderboard.port;

import com.terstredisproject1.domain.model.leaderboard.LeaderboardUserInfo;

import java.util.List;

public interface GetLeaderboardPort {

    long getRank(long userId);

    List<LeaderboardUserInfo> getTopUsers(int limit, int offset);

    long getScore(long userId);

    long incrementScore(long userId);
}
