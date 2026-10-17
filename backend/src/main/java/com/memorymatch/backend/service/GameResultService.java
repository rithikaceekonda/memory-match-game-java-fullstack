
package com.memorymatch.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorymatch.backend.dto.GameResultResponse;
import com.memorymatch.backend.entity.GameResult;
import com.memorymatch.backend.entity.Player;
import com.memorymatch.backend.repository.GameResultRepository;
import com.memorymatch.backend.repository.PlayerRepository;

@Service
public class GameResultService {

    private final GameResultRepository gameResultRepository;
    private final PlayerRepository playerRepository;

    public GameResultService(
            GameResultRepository gameResultRepository,
            PlayerRepository playerRepository) {
        this.gameResultRepository = gameResultRepository;
        this.playerRepository = playerRepository;
    }

    public GameResult saveGameResult(
            Long playerId, int score, int moves, boolean completed) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new java.util.NoSuchElementException(
                                "Player not found with ID: " + playerId));

        GameResult gameResult =
                new GameResult(player, score, moves, completed);

        return gameResultRepository.save(gameResult);
    }

    @Transactional(readOnly = true)
    public List<GameResultResponse> getResultsForPlayer(Long playerId) {
        return gameResultRepository.findByPlayerId(playerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameResultResponse> getLeaderboard() {
        return gameResultRepository.findAll()
                .stream()
                .sorted((first, second) ->
                        Integer.compare(
                                second.getScore(), first.getScore()))
                .map(this::toResponse)
                .toList();
    }

    private GameResultResponse toResponse(GameResult result) {
        return new GameResultResponse(
                result.getId(),
                result.getPlayer().getId(),
                result.getPlayer().getName(),
                result.getScore(),
                result.getMoves(),
                result.isCompleted(),
                result.getPlayedAt()
        );
    }
}
