
package com.memorymatch.backend.controller;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.memorymatch.backend.dto.GameResultResponse;
import com.memorymatch.backend.entity.GameResult;
import com.memorymatch.backend.service.GameResultService;

@RestController
@RequestMapping("/api/game-results")
public class GameResultController {

    private final GameResultService gameResultService;

    public GameResultController(GameResultService gameResultService) {
        this.gameResultService = gameResultService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameResult saveGameResult(
            @RequestBody Map<String, Object> request) {

        Long playerId = Long.valueOf(request.get("playerId").toString());
        int score = Integer.parseInt(request.get("score").toString());
        int moves = Integer.parseInt(request.get("moves").toString());
        boolean completed = Boolean.parseBoolean(
                request.get("completed").toString());

        if (score < 0 || moves < 0) {
            throw new IllegalArgumentException(
                    "Score and moves must not be negative");
        }

        return gameResultService.saveGameResult(
                playerId, score, moves, completed);
    }

    @GetMapping("/player/{playerId}")
    public List<GameResultResponse> getResultsForPlayer(
            @PathVariable Long playerId) {
        return gameResultService.getResultsForPlayer(playerId);
    }

    @GetMapping("/leaderboard")
    public List<GameResultResponse> getLeaderboard() {
        return gameResultService.getLeaderboard();
    }
}
