
package com.memorymatch.backend.dto;

import java.time.LocalDateTime;

public record GameResultResponse(
        Long id,
        Long playerId,
        String playerName,
        int score,
        int moves,
        boolean completed,
        LocalDateTime playedAt
) {
}
