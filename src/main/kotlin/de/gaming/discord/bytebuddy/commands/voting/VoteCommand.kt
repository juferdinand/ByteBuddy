package de.gaming.discord.bytebuddy.commands.voting

import de.gaming.discord.bytebuddy.commands.util.CommandUtil
import de.gaming.discord.bytebuddy.database.entity.GameVoting
import de.gaming.discord.bytebuddy.database.repos.DiscordUserRepository
import de.gaming.discord.bytebuddy.database.repos.GameRepository
import de.gaming.discord.bytebuddy.database.repos.GameVotingRepository
import de.gaming.discord.bytebuddy.database.repos.VotingRepository
import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import io.viascom.discord.bot.aluna.bot.queueAndRegisterInteraction
import io.viascom.discord.bot.aluna.model.EventRegisterType
import io.viascom.discord.bot.aluna.util.removeComponents
import io.viascom.discord.bot.aluna.util.replyStringChoices
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import java.util.*
import java.util.stream.Collectors

@Interaction

class VoteCommand(
    private val votingRepository: VotingRepository,
    private val gameRepository: GameRepository,
    private val discordUserRepository: DiscordUserRepository,
    private val gameVotingRepository: GameVotingRepository,
) : DiscordCommand(
    "vote",
    "Vote für ein Spiel in einem spezifischen Voting",
    observeAutoComplete = true
) {

    var votingId: Int = 0
    val votings = votingRepository.findAllByVotingTimeIsNotInPast()
    override fun initCommandOptions() {
        this.addOption(
            OptionType.STRING,
            "voting",
            "Das Voting wofür du stimmen möchtest",
            true,
            true
        )
        //this.addOption(OptionType.STRING, "spiel", "Das Spiel wofür du stimmen möchtest",  true, true)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        this.votingId = event.getOption("voting")!!.asInt
        val voting = votingRepository.findVotingByVotingId(votingId)
        if (voting == null) {
            event.reply("Das Voting $votingId existiert nicht").queue()
            return
        }
        val games = voting.gamesOfVoting
        val gamesMenu: List<SelectOption> =
            games.stream().map { SelectOption.of(it.gameName, it.gameId.toString()) }
                .collect(Collectors.toList())
        StringSelectMenu.create("games")
            .addOptions(gamesMenu)
            .build()
        event.reply("Wähle das Spiel aus wofür du Voten möchtest")
            .addActionRow(
                StringSelectMenu.create("games")
                    .addOptions(gamesMenu)
                    .build()
            ).setEphemeral(true)
            .queueAndRegisterInteraction(this, arrayListOf(EventRegisterType.STRING_SELECT))
    }

    override fun onAutoCompleteEvent(option: String, event: CommandAutoCompleteInteractionEvent) {
        when (option) {
            "voting" -> {
                event.replyStringChoices(
                    votings.filter {
                        it.votingId.toString().contains(event.focusedOption.value.lowercase())
                    }.map { it.votingId }.associate { it.toString() to it.toString() }

                ).queue()
            }
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent): Boolean {
        val selectedGameId = event.selectedOptions[0].value
        val game = gameRepository.findGameByGameId(selectedGameId.toInt())!!
        val voting = votingRepository.findVotingByVotingId(this.votingId)!!
        val discordUser =
            CommandUtil.getOrCreateAndGetDiscordUser(
                discordUserRepository, event.user,
                event.member!!
            )
        if (voting.votingTime!!.before(Date())) {
            event.editMessage("Das Voting ist bereits abgelaufen").removeComponents().queue()
            return false
        }
        if (gameVotingRepository.findGameVotingByDiscordUserIdAndVotingId(
                discordUser,
                voting
            ) != null
        ) {
            event.editMessage("Du hast bereits für dieses Voting abgestimmt").removeComponents()
                .queue()
            return false
        }
        try {
            gameVotingRepository.save(GameVoting(game, voting, discordUser))
            event.guild!!.textChannels.stream().filter { it.idLong == voting.targetChannelId }
                .findFirst().get().editMessageEmbedsById(
                    voting.votingMessageId!!,
                    CommandUtil.createVotingEmbed(
                        voting,
                        CommandUtil.createVotesMaps(gameVotingRepository.findAllByVotingId(voting))
                    )
                ).queue()

            event.editMessage("Du hast erfolgreich für ${game.gameName} abgestimmt")
                .removeComponents()
                .queue()
        } catch (e: Exception) {
            event.editMessage("Es ist ein interner Fehler aufgetreten versuche es bitte später erneut")
                .removeComponents().queue()
            return false
        }
        return true
    }
}