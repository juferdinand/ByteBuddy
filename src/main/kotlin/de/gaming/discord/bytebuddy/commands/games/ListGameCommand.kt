package de.gaming.discord.bytebuddy.commands.games

import de.gaming.discord.bytebuddy.database.entity.Game
import de.gaming.discord.bytebuddy.database.repos.GameRepository
import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

@Interaction
class ListGameCommand(private val gameRepository: GameRepository) :
    DiscordCommand("list-game", "Liste alle Spiele auf") {
    override fun execute(event: SlashCommandInteractionEvent) {
        val games = gameRepository.findAll()
        val stringBuilder = StringBuilder()
        games.forEach {
            stringBuilder.append("${it.gameName} - ${it.gameReason} - ${it.gameUrl} - ${it.gameImage} - ${it.gamePrice}\n")
        }
        event.replyEmbeds(createEmbed(games)).queue()
    }

    private fun createEmbed(games: List<Game>): List<MessageEmbed> {
        val embeds = mutableListOf<MessageEmbed>()
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
        return embeds
    }

    private fun initEmbedContent(embedBuilder: EmbedBuilder, game: Game) {
        // Custom Emojis für den Kaufstatus
        val boughtEmoji = if (game.gameBought) ":white_check_mark:" else ":x:" // Ersetzen mit den entsprechenden Custom Emojis
        val priceEmoji = ":moneybag:" // Ersetzen mit einem passenden Custom Emoji

        // Umstrukturierung der Felder
        embedBuilder.addField("📝 Id", game.gameId.toString(), false)
        embedBuilder.addField("🔗 Store", "[Link zum Spiel](${game.gameUrl})", false)
        embedBuilder.addField("$priceEmoji Preis", "${game.gamePrice.toPlainString().replace(".", ",")} €", false)
        embedBuilder.addField("$boughtEmoji Gekauft", "", false)
        if (!game.gameBought) {
            embedBuilder.addField("🤔 Grund", game.gameReason, false)
        }
    }

}