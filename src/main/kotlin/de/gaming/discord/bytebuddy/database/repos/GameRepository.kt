package de.gaming.discord.bytebuddy.database.repos

import de.gaming.discord.bytebuddy.database.entity.Game
import org.springframework.data.jpa.repository.JpaRepository

interface GameRepository: JpaRepository<Game, Int> {

    fun findGameByGameId(gameId: Int): Game?
    fun existsByGameName(asString: String?): Boolean
}