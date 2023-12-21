package de.gaming.discord.bytebuddy.commands.games

import de.gaming.discord.bytebuddy.database.entity.Game
import de.gaming.discord.bytebuddy.database.repos.GameRepository
import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import io.viascom.discord.bot.aluna.bot.queueAndRegisterInteraction
import io.viascom.discord.bot.aluna.model.EventRegisterType
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color

@Interaction
class ListGameCommand(private val gameRepository: GameRepository) :
    DiscordCommand("list-game", "Liste alle Spiele auf") {

    private val nextSite = Button.secondary("next-site", "Nächste Seite")
    private val previousSite = Button.secondary("previous-site", "Vorherige Seite")

    private val pageEmbeds = mutableMapOf<Int, Collection<MessageEmbed>>()
    private var currentPage = 1
    override fun execute(event: SlashCommandInteractionEvent) {
        event.reply("Die Spiele werden geladen...").setEphemeral(true).queue()
        val games = gameRepository.findAll()
        if (games.isEmpty()) {
            event.reply("Es sind keine Spiele in der Liste").setEphemeral(true).queue()
            return
        }
        val stringBuilder = StringBuilder()
        games.forEach {
            stringBuilder.append("${it.gameName} - ${it.gameReason} - ${it.gameUrl} - ${it.gameImage} - ${it.gamePrice}\n")
        }
        createEmbeds(games)

        if (pageEmbeds.size > 1) {
            event.hook.editOriginal("").setEmbeds(
                (pageEmbeds[currentPage - 1]!!)
            ).setActionRow(nextSite)
                .queueAndRegisterInteraction(
                    event.hook,
                    this,
                    arrayListOf(EventRegisterType.BUTTON)
                )
            return
        }
        event.hook.editOriginal("").setEmbeds(pageEmbeds[currentPage - 1]!!).queue()
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent): Boolean {
        if (event.componentId == "next-site") {
            currentPage++
        }
        if (event.componentId == "previous-site") {
            currentPage--
        }

        val neededActionRow = if (currentPage == pageEmbeds.size) {
            listOf(previousSite)
        } else if (currentPage == 1) {
            listOf(nextSite)
        } else {
            listOf(previousSite, nextSite)
        }

        event.editMessageEmbeds(pageEmbeds[currentPage - 1]!!).setActionRow(neededActionRow)
            .queueAndRegisterInteraction(
                event.hook,
                this,
                arrayListOf(EventRegisterType.BUTTON)
            )

        return true
    }

    private fun createEmbeds(games: List<Game>) {
        val embeds = mutableListOf<MessageEmbed>()
        var page = 0
        for (game in games) {
            val embedBuilder = EmbedBuilder()
            embedBuilder.setColor(Color.decode("#7289DA")) // Wählen Sie eine bevorzugte Farbe
            embedBuilder.setTitle("Spiel Details: ${game.gameName}")
            initEmbedContent(embedBuilder, game)
            embedBuilder.setImage(game.gameImage)
            embedBuilder.setFooter("Erstellt von ${game.createdBy?.discordUserName}", game.createdBy?.avatarUrl)
            embedBuilder.setTimestamp(game.createdDate)
            embeds.add(embedBuilder.build())
        }

        embeds.chunked(10).forEach {
            pageEmbeds[page] = it
            page++
        }
    }

    private fun initEmbedContent(embedBuilder: EmbedBuilder, game: Game) {
        // Custom Emojis für den Kaufstatus
        val boughtEmoji = if (game.gameBought) ":white_check_mark:" else ":x:" // Ersetzen mit den entsprechenden Custom Emojis
        val priceEmoji = ":moneybag:" // Ersetzen mit einem passenden Custom Emoji

        // Umstrukturierung der Felder
        embedBuilder.addField("📝 Id", game.gameId.toString(), false)
        embedBuilder.addField("🔗 Store", "[Link zum Spiel](${game.gameUrl})", false)
        embedBuilder.addField(
            "$priceEmoji Preis",
            if (game.gamePrice.toPlainString()
                    .equals("-1.00")
            ) "Aktuell noch Unbekannt" else game.gamePrice.toPlainString().replace(".", ",").plus(" €"),
            false
        )
        if (game.gameReleaseDateText != null) {
            embedBuilder.addField("📅 Release", game.gameReleaseDateText, false)
        }
        embedBuilder.addField("$boughtEmoji Gekauft", "", false)
        if (!game.gameBought) {
            embedBuilder.addField("🤔 Grund", game.gameReason, false)
        }
    }

}