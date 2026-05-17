package com.terstredisproject1.adapter.controller.leaderboard;

import com.terstredisproject1.domain.model.leaderboard.LeaderboardUserInfo;
import com.terstredisproject1.usecase.leaderboard.GetLeaderboardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaderboardController {
    private final GetLeaderboardUseCase getLeaderboardUseCase;

    @GetMapping("/leaderboard")
    public List<LeaderboardUserInfo> getTopUsers(@RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "0") Integer offset) {
        return getLeaderboardUseCase.getTopUsers(limit, offset);
    }

    @GetMapping("/leaderboard/user/{userId}/score")
    public Long getScore(@PathVariable long userId) {
        return getLeaderboardUseCase.getScore(userId);
    }

    @GetMapping("/leaderboard/user/{userId}/rank")
    public Long getRank(@PathVariable long userId) {
        return getLeaderboardUseCase.getRank(userId);
    }

    @GetMapping("/leaderboard/user/{userId}/increment")
    public Long incrementScore(@PathVariable long userId) {
        return getLeaderboardUseCase.incrementScore(userId);
    }
}
