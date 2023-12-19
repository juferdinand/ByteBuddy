package de.gaming.discord.bytebuddy.commands

import de.gaming.discord.bytebuddy.commands.util.CommandUtil
import de.gaming.discord.bytebuddy.database.entity.DiscordUser
import de.gaming.discord.bytebuddy.database.entity.Game
import de.gaming.discord.bytebuddy.database.repos.DiscordUserRepository
import de.gaming.discord.bytebuddy.database.repos.GameRepository
import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Interaction
class AddGameCommand(
    private val gameRepository: GameRepository,
    private val discordUserRepository: DiscordUserRepository
) : DiscordCommand("add-game", "Füge das Spiel zu unserer Spieleliste hinzu") {

    override fun initCommandOptions() {
        this.addOption(OptionType.STRING, "name", "Der Name vom Spiel", true)
        this.addOption(OptionType.STRING, "grund", "Der Grund warum das Spiel benötigt wird", true)
        this.addOption(OptionType.STRING, "store", "Die URL zum Spiel", true)
        this.addOption(OptionType.STRING, "bild", "Die URL zum Bild des Spiels", true)
        this.addOption(OptionType.STRING, "preis", "Der Preis des Spiels ohne €", true)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val discordUser = CommandUtil.getOrCreateAndGetDiscordUser(discordUserRepository, event.user)
        if (gameRepository.existsByGameName(event.getOption("name")?.asString)) {
            event.reply("Das Spiel ${event.getOption("name")?.asString} ist bereits in der Liste").complete()
            return
        }
        checkIfUrlsAreValid(event)
        val game = Game()
        game.gameName = event.getOption("name")?.asString
        game.gameReason = event.getOption("grund")?.asString
        game.gameUrl = event.getOption("store")?.asString
        game.gameImage = event.getOption("bild")?.asString
        game.gamePrice = event.getOption("preis")?.asString?.replace(",",".")?.toBigDecimal()
        game.createdBy = discordUser
        gameRepository.save(game)
        event.reply("Das Spiel ${game.gameName} wurde erfolgreich hinzugefügt").complete()
    }

    private fun checkIfUrlsAreValid(event: SlashCommandInteractionEvent) {
        val storeUrl = event.getOption("store")?.asString
        val imageUrl = event.getOption("bild")?.asString
        if (!storeUrl!!.startsWith("https://")) {
            event.reply("Die Store URL ist nicht gültig").complete()
        }
        if (!imageUrl!!.startsWith("https://")) {
            event.reply("Die Bild URL ist nicht gültig").complete()
        }
    }
}