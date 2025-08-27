package com.bnz.soccer.data.repository;

import com.bnz.soccer.data.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
