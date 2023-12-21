package de.gaming.discord.bytebuddy.commands.games

import de.gaming.discord.bytebuddy.commands.games.scrapper.steam.SteamStoreScrapper
import de.gaming.discord.bytebuddy.commands.games.scrapper.epic.EpicgamesStoreScrapper
import de.gaming.discord.bytebuddy.commands.util.CommandUtil
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
        this.addOption(OptionType.STRING, "grund", "Der Grund warum das Spiel benötigt wird", true)
        this.addOption(OptionType.STRING, "store", "Die URL zum Spiel", true)
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        event.reply("Die Daten werden aus dem Store extrahiert...").setEphemeral(true).queue()
        val gameUrl = event.getOption("store")?.asString!!
        val storeScrapperResult = if (gameUrl.contains("epicgames")) {
            EpicgamesStoreScrapper.scrap(gameUrl)
        } else if (gameUrl.contains("steam")) {
            SteamStoreScrapper.scrap(gameUrl)
        } else {
            event.hook.editOriginal("Der Store wird nicht unterstützt").queue()
            return
        }

        val discordUser = CommandUtil.getOrCreateAndGetDiscordUser(
            discordUserRepository,
            event.user,
            event.member!!
        )
        event.hook.editOriginal("Das Spiel wird hinzugefügt...").queue()
        if (gameRepository.existsByGameName(event.getOption("name")?.asString)) {
            event.hook.editOriginal("Das Spiel ${event.getOption("name")?.asString} ist bereits in der Liste")
                .queue()
            return
        }

        val game = Game()
        game.gameName = storeScrapperResult.title
        game.gameReason = event.getOption("grund")?.asString
        game.gameUrl = event.getOption("store")?.asString
        game.gameImage = storeScrapperResult.imageUrl
        game.gamePrice = storeScrapperResult.price
        game.gameReleaseDateText = storeScrapperResult.releaseDateText
        game.createdBy = discordUser
        gameRepository.save(game)
        event.hook.editOriginal(
            "Das Spiel ${game.gameName} wurde erfolgreich hinzugefügt"
        ).queue()
    }
}