package de.gaming.discord.bytebuddy.commands.voting

import de.gaming.discord.bytebuddy.commands.util.CommandUtil
import de.gaming.discord.bytebuddy.database.entity.Game
import de.gaming.discord.bytebuddy.database.entity.Voting
import de.gaming.discord.bytebuddy.database.entity.VotingReason
import de.gaming.discord.bytebuddy.database.repos.DiscordUserRepository
import de.gaming.discord.bytebuddy.database.repos.GameRepository
import de.gaming.discord.bytebuddy.database.repos.VotingRepository
import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import io.viascom.discord.bot.aluna.bot.queueAndRegisterInteraction
import io.viascom.discord.bot.aluna.model.EventRegisterType
import io.viascom.discord.bot.aluna.util.removeComponents
import io.viascom.discord.bot.aluna.util.replyStringChoices
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

@Interaction
class StartVotingCommand(
    private val votingRepository: VotingRepository,
    private val discordUserRepository: DiscordUserRepository,
    private val gameRepository: GameRepository
) :
    DiscordCommand("start-voting", "Starte eine Abstimmung", observeAutoComplete = true) {

    private var voting: Voting? = null
    private var targetChannel: MessageChannel? = null
    private val gamesInVoting = mutableSetOf<Game>()
    override fun initCommandOptions() {
        this.addOption(OptionType.STRING, "name", "Der Name der Abstimmung", true)
        this.addOption(OptionType.STRING, "grund", "Der Grund der Abstimmung", true, true)
        this.addOption(
            OptionType.STRING,
            "endzeit",
            "Die Endzeit der Abstimmung (Format: 12:34)",
            true
        )
        this.addOption(
            OptionType.STRING,
            "enddatum",
            "Das Enddatum der Abstimmung (Format: 01.01.2024)",
            false
        )
        this.addOption(
            OptionType.CHANNEL,
            "kanal",
            "Der Kanal in dem die Abstimmung stattfinden soll",
            false
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val discordUser =
            CommandUtil.getOrCreateAndGetDiscordUser(
                discordUserRepository, event.user,
                event.member!!
            )
        targetChannel =
            event.getOption("kanal")?.asChannel?.asGuildMessageChannel() ?: event.channel

        val votingReason = try {
            VotingReason.valueOf(event.getOption("grund")!!.asString)
        } catch (e: IllegalArgumentException) {
            event.reply("Der Grund ${event.getOption("grund")!!.asString} ist nicht verfügbar")
                .setEphemeral(true).queue()
            return
        }
        val voting = Voting()
        voting.votingName = event.getOption("name")!!.asString
        voting.votingReason = votingReason
        voting.targetChannelId = targetChannel!!.idLong
        voting.createdBy = discordUser
        voting.votingTime = CommandUtil.transformTime(
            event.getOption("endzeit")!!.asString,
            event.getOption("enddatum")?.asString
        )
        if (voting.votingTime!!.before(Date())) {
            event.reply("Das Enddatum darf nicht in der Vergangenheit liegen").setEphemeral(true)
                .queue()

            return
        }
        votingRepository.save(voting)
        this.voting = voting

        event.reply("Wähle Spiele für die Abstimmung:").setEphemeral(true)
            .setActionRow(
                createStringSelectMenu(),
            ).addActionRow(Button.danger("cancel-voting", "Voting abbrechen"))
            .queueAndRegisterInteraction(
                this,
                arrayListOf(EventRegisterType.STRING_SELECT, EventRegisterType.BUTTON)
            )
    }

    private fun createStringSelectMenu(): StringSelectMenu {
        val gameOptions: List<SelectOption> = gameRepository.findAll().stream()
            .filter { game -> gamesInVoting.stream().noneMatch { gameInVoting -> gameInVoting.gameId == game.gameId } }
            .map { game -> SelectOption.of(game.gameName, game.gameId.toString()) }
            .collect(Collectors.toList())

        return StringSelectMenu.create("games-menu")
            .setPlaceholder("Spiel auswählen  (Aktuell ausgwählt: ${gamesInVoting.size})")
            .addOptions(gameOptions)
            .build()
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent): Boolean {
        val selectedGameId = event.selectedOptions[0].value
        val game = gameRepository.findGameByGameId(selectedGameId.toInt())
        if (game == null) {
            event.editMessage("Es ist ein Fehler aufgetreten, bitte versuche es erneut")
                .queueAndRegisterInteraction(
                    event.hook,
                    this,
                    arrayListOf(EventRegisterType.STRING_SELECT, EventRegisterType.BUTTON)
                )
            return true
        }
        gamesInVoting.add(game)

        if (gamesInVoting.size < 2) {
            event.editSelectMenu(createStringSelectMenu())
                .queueAndRegisterInteraction(
                    event.hook,
                    this,
                    arrayListOf(EventRegisterType.STRING_SELECT, EventRegisterType.BUTTON)
                )
            return true
        }
        event.editMessage("Möchtest du ein weiteres Spiel hinzufügen oder das Voting starten?")
            .removeComponents().setActionRow(
                Button.primary("add-more", "Weiteres Spiel hinzufügen"),
                Button.success("start-voting", "Voting starten"),
                Button.danger("cancel-voting", "Voting abbrechen")
            ).queueAndRegisterInteraction(
                event.hook,
                this,
                arrayListOf(EventRegisterType.BUTTON)
            )
        return true
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent): Boolean {
        when (event.componentId) {
            "add-more" -> {
                event.editMessage("Wähle Spiele für die Abstimmung:").removeComponents()
                    .setComponents(
                        ActionRow.of(createStringSelectMenu()),
                        ActionRow.of(Button.danger("cancel-voting", "Voting abbrechen"))
                    ).queueAndRegisterInteraction(
                        event.hook,
                        this,
                        arrayListOf(EventRegisterType.STRING_SELECT, EventRegisterType.BUTTON)
                    )
                return true
            }
            "start-voting" -> {
                try {
                    this.voting!!.gamesOfVoting = gamesInVoting
                    votingRepository.save(this.voting!!)
                } catch (e: Exception) {
                    event.editMessage("Es ist ein Fehler aufgetreten, bitte versuche es erneut")
                        .removeComponents().queue()
                    event.message.delete().queueAfter(3, TimeUnit.SECONDS)
                    e.printStackTrace()
                    return false
                }
                event.editMessage("Das Voting wird gestartet").removeComponents().queue()
                event.message.delete().queueAfter(3, TimeUnit.SECONDS)



                var messageId: Long
                targetChannel!!.sendMessageEmbeds(CommandUtil.createVotingEmbed(voting, gamesInVoting.stream().map { game -> game.gameId to "0"}.collect(Collectors.toMap({it.first}, {it.second})))).queue{
                    messageId = it.idLong
                    voting!!.votingMessageId = messageId
                    votingRepository.save(voting!!)
                };

                return true
            }
        }
        return true
    }



    override fun onAutoCompleteEvent(option: String, event: CommandAutoCompleteInteractionEvent) {
        val options = hashMapOf<String, String>()

        options["Neue Spiele"] = VotingReason.NEW_GAMES.name
        options["Heutige Spiele"] = VotingReason.TODAY_GAMES.name

        event.replyStringChoices(options.filter {
            it.key.lowercase().contains(event.focusedOption.value.lowercase())
        }).queue()
    }
}