package de.gaming.discord.bytebuddy.commands.user

import de.gaming.discord.bytebuddy.commands.util.CommandUtil
import de.gaming.discord.bytebuddy.database.entity.DiscordUser
import de.gaming.discord.bytebuddy.database.repos.DiscordUserRepository
import de.gaming.discord.bytebuddy.database.repos.GameRepository
import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import io.viascom.discord.bot.aluna.util.replyStringChoices
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Interaction
class AddGameToProfileCommand(
    private val discordUserRepository: DiscordUserRepository,
    private val gameRepository: GameRepository
) : DiscordCommand(
    "add-game-to-profile", "Fügt deinem Profil ein Spiel hinzu", observeAutoComplete = true
) {

    private var discordUser: DiscordUser? = null
    private val games = gameRepository.findAll()

    init {
        this.addOption(
            OptionType.STRING, "game", "Das Spiel welches du hinzufügen möchtest", true, true
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        discordUser!!.addGame(
            gameRepository.findGameByGameId(event.getOption("game")?.asString!!.toInt())
                ?: return event.reply("Das Spiel konnte nicht gefunden werden").setEphemeral(true)
                    .queue()
        )
        discordUserRepository.save(discordUser!!)
        event.reply("Das Spiel wurde erfolgreich deinem Profil hinzugefügt").setEphemeral(true).queue()
    }

    override fun onAutoCompleteEvent(option: String, event: CommandAutoCompleteInteractionEvent) {
        if (option == "game") {
            val input = event.focusedOption.value.lowercase()
            if (discordUser == null) {
                discordUser = CommandUtil.getOrCreateAndGetDiscordUser(
                    discordUserRepository, event.user, event.member!!, true
                )
            }

            event.replyStringChoices(games
                .filter { game -> discordUser!!.games.find { it.gameId == game.gameId } == null }
                .filter { game -> game.gameName.lowercase().contains(input) }
                .associate { it.gameName to it.gameId.toString() }).queue()
        }
    }
}