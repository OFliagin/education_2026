package com.terstredisproject1.domain.model.leaderboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LeaderboardUserInfo {
    int rank;
    long userId;
    String username;
    int score;
}
