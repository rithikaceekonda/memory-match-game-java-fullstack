
package com.memorymatch.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.memorymatch.backend.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByName(String name);
}
