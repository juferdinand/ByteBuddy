package de.gaming.discord.bytebuddy.database.repos

import de.gaming.discord.bytebuddy.database.entity.DiscordUser
import de.gaming.discord.bytebuddy.database.entity.Game
import de.gaming.discord.bytebuddy.database.entity.GameVoting
import de.gaming.discord.bytebuddy.database.entity.Voting
import org.springframework.data.jpa.repository.JpaRepository

interface GameVotingRepository:JpaRepository<GameVoting,String> {

    fun findGameVotingByVotingId(votingId: Voting): List<GameVoting>

    fun findGameVotingByGameVotingId(gameVotingId: String): GameVoting?
    fun findGameVotingByDiscordUserIdAndVotingId(discordUserId:DiscordUser, votingId: Voting): GameVoting?
    fun findAllByVotingId(voting: Voting): List<GameVoting>

    fun countByVotingIdAndGameId(votingId: Voting, gameId: Game): Int
    fun countByVotingId(voting: Voting): Int
}