package de.gaming.discord.bytebuddy.commands.user

import de.gaming.discord.bytebuddy.commands.user.data.SocialMediaPlatform
import de.gaming.discord.bytebuddy.database.entity.DiscordUser
import de.gaming.discord.bytebuddy.database.repos.DiscordUserRepository
import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import io.viascom.discord.bot.aluna.bot.queueAndRegisterInteraction
import io.viascom.discord.bot.aluna.model.EventRegisterType
import io.viascom.discord.bot.aluna.util.removeComponents
import io.viascom.discord.bot.aluna.util.setColor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

@Interaction
class ProfileCommand(private val discordUserRepository: DiscordUserRepository) :
    DiscordCommand("profile", "Zeigt dein Profil oder das von anderen an") {
    final var discordUser: DiscordUser? = null
    override fun initCommandOptions() {
        this.addOption(
            OptionType.USER,
            "user",
            "Der User von dem du das Profil sehen möchtest",
            false
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val user = event.getOption("user")?.asUser ?: event.user
        this.discordUser = discordUserRepository.findDiscordUserAndLeftJoinFetchGames(user.idLong)

        if (discordUser == null) {
            return event.reply("Der User ${user.name} konnte nicht in der Datenbank gefunden werden")
                .setEphemeral(true).queue()
        }
        event.replyEmbeds(
            createProfileEmbed(
                discordUser!!,
                discordUser!!.discordUserId == event.user.idLong
            )
        ).addActionRow(Button.primary("games", "Zeige alle Spiele des Benutzers an"))
            .queueAndRegisterInteraction(this, arrayListOf(EventRegisterType.BUTTON))
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent): Boolean {
        if (event.componentId == "games") {
            val user = event.user
            if (discordUser == null) {
                event.editMessage("Der User ${user.name} konnte nicht in der Datenbank gefunden werden")
                    .removeComponents()
                    .queue()
                event.message.delete().queueAfter(3, TimeUnit.SECONDS)
                return true
            }
            event.editMessageEmbeds(
                createProfileEmbed(
                    discordUser!!,
                    this.discordUser!!.discordUserId == event.user.idLong,
                    true
                )
            ).removeComponents().queue()
            return true
        }
        return false
    }

    private fun createProfileEmbed(
        discordUser: DiscordUser,
        ownProfile: Boolean,
        showGames: Boolean = false
    ): MessageEmbed {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm")

        val embedBuilder = EmbedBuilder()
        embedBuilder.setTitle(if (ownProfile) "Dein Profil" else "Profil von ${discordUser.discordUserName}")
        embedBuilder.setThumbnail(discordUser.avatarUrl)
        embedBuilder.setColor(
            discordUser.favoriteColor ?: "#95e2fc"
        ) // Stellen Sie sicher, dass die Farbe ein int ist.

        embedBuilder.addField("📝 Username", discordUser.discordUserName, false)
        if (showGames) {
            val games = discordUser.games.joinToString("\n") { "- ${it.gameName}" }
            embedBuilder.addField("🎮 Spiele", "```$games```", false)
        } else {
            embedBuilder.addField("🎮 Spiele (Anzahl)", discordUser.games.size.toString(), false)
        }

        if (discordUser.joinedAt != null) {
            embedBuilder.addField("📅 Server beigetreten", sdf.format(discordUser.joinedAt.toEpochMilli()), false)
        }
        // Social Media
        val socialMediaUrls = mapOf(
            "Spotify" to discordUser.spotifyPlaylistUrl,
            "Twitter" to discordUser.twitterChannelUrl,
            "Twitch" to discordUser.twitchChannelUrl,
            "YouTube" to discordUser.youtubeChannelUrl,
            "Instagram" to discordUser.instagramChannelUrl,
            "TikTok" to discordUser.tiktokChannelUrl
        )

        for (platform in SocialMediaPlatform.allAvailable()) {
            socialMediaUrls[platform.platform.platformName]?.let { url ->
                embedBuilder.addField(
                    "${platform.emoji} ${platform.platform.platformName}",
                    "[${platform.platform.platformName}]($url)",
                    true
                )
            }
        }
        return embedBuilder.build()
    }
}