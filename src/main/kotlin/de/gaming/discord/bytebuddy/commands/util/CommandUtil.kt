package de.gaming.discord.bytebuddy.commands.util

import de.gaming.discord.bytebuddy.database.entity.DiscordUser
import de.gaming.discord.bytebuddy.database.entity.GameVoting
import de.gaming.discord.bytebuddy.database.entity.Voting
import de.gaming.discord.bytebuddy.database.repos.DiscordUserRepository
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

class CommandUtil {
    companion object {
        fun getOrCreateAndGetDiscordUser(
            discordUserRepository: DiscordUserRepository,
            user: User
        ): DiscordUser {
            var discordUser = discordUserRepository.findDiscordUserByDiscordUserId(user.idLong)
            if (discordUser == null) {
                discordUser = DiscordUser()
                discordUser.discordUserId = user.idLong
                discordUser.discordUserName = user.name
                discordUser.avatarUrl = user.avatarUrl?:"https://i.imgur.com/Euv3KE5.png"
                discordUserRepository.save(discordUser)
            }
            return discordUser
        }

        fun transformTime(endTime: String, endDate: String?): Date {
            val sdfDate = SimpleDateFormat("dd.MM.yyyy")
            val endDateObject =
                if (endDate == null) {
                    Date()
                } else {
                    sdfDate.parse(endDate)
                }

            //fusion endDate and endTime
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm")
            return sdf.parse("${sdfDate.format(endDateObject)} $endTime")
        }

        fun createVotingEmbed(voting:Voting?, votes:Map<Int, String>): MessageEmbed {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm")
            val embedBuilder = EmbedBuilder()
            embedBuilder.setTitle("Voting: ${voting!!.votingName}")
            embedBuilder.setDescription(voting.votingReason!!.desc)
            embedBuilder.setFooter(
                "Erstellt von ${voting.createdBy?.discordUserName}",
                voting.createdBy?.avatarUrl
            )
            embedBuilder.setThumbnail("https://cdn.discordapp.com/attachments/1186640863581970514/1186640885723709471/Voting_Icon.png?ex=6593fc71&is=65818771&hm=13ba485f073c4eeb83367b548cdc272757609c268740c77ce2095e14be1327ab&")
            embedBuilder.setTimestamp(voting.createdAt)
            embedBuilder.setColor(0x7289DA)
            embedBuilder.addField("Voting ID", voting.votingId.toString(), false)
            embedBuilder.addField("Endzeit", sdf.format(voting.votingTime), false)
            embedBuilder.addField(
                "Spiele",
                voting.gamesOfVoting!!.stream().map { game -> game.gameName + "(${votes[game.gameId]?:0})" }
                    .collect(Collectors.joining("\n")),
                false)
            return embedBuilder.build()
        }

        fun createVotesMaps(gameVotings: List<GameVoting>): Map<Int, String> {
            val votes = mutableMapOf<Int, String>()
            gameVotings.forEach {
                votes[it.gameId.gameId] = votes.getOrDefault(it.gameId.gameId, "0").toInt().plus(1).toString()
            }
            return votes
        }
    }
}