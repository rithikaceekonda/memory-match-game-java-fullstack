
package com.memorymatch.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.memorymatch.backend.entity.GameResult;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {

    List<GameResult> findByPlayerId(Long playerId);

    List<GameResult> findByPlayerIdOrderByScoreDesc(Long playerId);
}
