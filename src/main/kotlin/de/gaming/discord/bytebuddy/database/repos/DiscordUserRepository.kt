package de.gaming.discord.bytebuddy.database.repos

import de.gaming.discord.bytebuddy.database.entity.DiscordUser
import org.springframework.data.jpa.repository.JpaRepository

interface DiscordUserRepository : JpaRepository<DiscordUser, Long> {

    fun findDiscordUserByDiscordUserId(discordId: Long): DiscordUser?
}