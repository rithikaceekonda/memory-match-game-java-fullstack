
package com.memorymatch.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_results")
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "score", nullable = false)
    private int score = 0;

    @Column(name = "moves", nullable = false)
    private int moves = 0;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "played_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime playedAt;

    public GameResult() {
    }

    public GameResult(Player player, int score, int moves, boolean completed) {
        this.player = player;
        this.score = score;
        this.moves = moves;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }
}
