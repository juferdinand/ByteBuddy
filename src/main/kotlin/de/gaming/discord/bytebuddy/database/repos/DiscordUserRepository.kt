package de.gaming.discord.bytebuddy.database.repos

import de.gaming.discord.bytebuddy.database.entity.DiscordUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DiscordUserRepository : JpaRepository<DiscordUser, Long> {

    fun findDiscordUserByDiscordUserId(discordId: Long): DiscordUser?

    @Query("SELECT d FROM DiscordUser d LEFT JOIN FETCH d.games WHERE d.discordUserId = :idLong")
    fun findDiscordUserAndLeftJoinFetchGames(idLong: Long): DiscordUser?
}