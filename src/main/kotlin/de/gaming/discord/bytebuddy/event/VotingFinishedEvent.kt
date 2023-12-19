package de.gaming.discord.bytebuddy.event

import de.gaming.discord.bytebuddy.database.entity.Game
import de.gaming.discord.bytebuddy.database.entity.Voting
import de.gaming.discord.bytebuddy.database.repos.GameVotingRepository
import de.gaming.discord.bytebuddy.database.repos.VotingRepository
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.sharding.ShardManager
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class VotingFinishedEvent(
    private val votingRepository: VotingRepository,
    private val gameVotingRepository: GameVotingRepository,
    private val shardManager: ShardManager
) {

    @Scheduled(cron = "0 * * * * ?")
    fun onVotingFinished() {
        val votings = votingRepository.findAllByVotingTimeIsInPast()
        for (voting in votings) {
            val channel =
                shardManager.shards[0].guilds[0].getTextChannelById(voting.targetChannelId)
                    ?: continue
            channel.editMessageEmbedsById(voting.votingMessageId, createVotingFinishedEmbed(voting))
                .queue()
        }
    }

    private fun createVotingFinishedEmbed(voting: Voting): MessageEmbed {
        val embedBuilder = EmbedBuilder()

        embedBuilder.setTitle("📊 Voting Ergebnisse")
        val countOfAllVotings = gameVotingRepository.countByVotingId(voting)
        embedBuilder.setDescription("Hier sind die Ergebnisse des Votings für '${voting.votingName}'. Insgesamt wurde${if (countOfAllVotings > 1) "n" else ""} $countOfAllVotings Stimme${if (countOfAllVotings > 1) "n" else ""} abgegeben:")
        embedBuilder.setColor(0x00FF00) // Eine grüne Farbe

        val firstThreeGamesWithVotingCount = getFirstThreeGamesWithVotingCount(voting)

        val emojis = listOf("🥇", "🥈", "🥉")
        var currentPlace = 0
        for (game in firstThreeGamesWithVotingCount.keys) {
            embedBuilder.addField(
                "${emojis[currentPlace]} Platz ${currentPlace + 1}: ${game.gameName}",
                "${firstThreeGamesWithVotingCount[game]} Stimme${if (firstThreeGamesWithVotingCount[game] == 1) "" else "n"}",
                false
            )
            currentPlace++
        }
        embedBuilder.setFooter("Voting abgeschlossen am: ${voting.votingTime}")

        return embedBuilder.build()

    }

    private fun getFirstThreeGamesWithVotingCount(voting: Voting): Map<Game, Int> {
        val games = voting.gamesOfVoting
        val gamesWithVotings = mutableMapOf<Game, Int>()
        for (game in games) {
            gamesWithVotings[game] = gameVotingRepository.countByVotingIdAndGameId(voting, game)
        }
        val sortedGamesWithVotings =
            gamesWithVotings.toList().sortedByDescending { (_, value) -> value }.toMap()
        val firstThreeGamesWithVotings = sortedGamesWithVotings.toList()
            .subList(0, if (gamesWithVotings.size < 3) gamesWithVotings.size else 3).toMap()
        return firstThreeGamesWithVotings
    }
}