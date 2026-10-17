
package com.memorymatch.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.memorymatch.backend.entity.Player;
import com.memorymatch.backend.repository.PlayerRepository;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player createPlayer(String name) {
        Player player = new Player(name);
        return playerRepository.save(player);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player getPlayerById(Long id) {
    return playerRepository.findById(id)
            .orElseThrow(() ->
                    new java.util.NoSuchElementException(
                            "Player not found with ID: " + id));
    }
}
