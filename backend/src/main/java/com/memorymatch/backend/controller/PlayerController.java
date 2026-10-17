
package com.memorymatch.backend.controller;

import com.memorymatch.backend.entity.Player;
import com.memorymatch.backend.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Player createPlayer(@RequestBody Map<String, String> request) {
        String name = request.get("name");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }

        return playerService.createPlayer(name.trim());
    }

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }
}
