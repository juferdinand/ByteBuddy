package de.gaming.discord.bytebuddy.commands.user

import de.gaming.discord.bytebuddy.commands.user.data.Platform
import de.gaming.discord.bytebuddy.commands.user.data.SocialMediaPlatform
import de.gaming.discord.bytebuddy.commands.util.CommandUtil
import de.gaming.discord.bytebuddy.database.repos.DiscordUserRepository
import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import io.viascom.discord.bot.aluna.util.replyStringChoices
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Interaction
class AddSocialMediaToProfileCommand(
    private val discordUserRepository: DiscordUserRepository
) : DiscordCommand(
    "set-social-media-to-profile",
    "Fügt oder ändert  in deinem Profil ein Social Media Account",
    observeAutoComplete = true
) {

    override fun initCommandOptions() {
        this.addOption(
            OptionType.STRING,
            "social-media-url",
            "Die URL zu deinem Social Media Account oder deiner Spotify Playlist",
            true
        )
        this.addOption(
            OptionType.STRING,
            "social-media-platform",
            "Der Name deines Social Media Accounts oder deiner Spotify Playlist",
            true,
            true
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val socialMediaUrl = event.getOption("social-media-url")?.asString!!
        val socialMediaPlatform = event.getOption("social-media-plattform")?.asString!!
        val platform = try {
            Platform.fromPlatformName(socialMediaPlatform)
        } catch (e: IllegalArgumentException) {
            event.reply("Die Plattform $socialMediaPlatform ist nicht verfügbar")
                .setEphemeral(true).queue()
            return
        }

        val discordUser = CommandUtil.getOrCreateAndGetDiscordUser(
            discordUserRepository,
            event.user,
            event.member!!
        )
        try {
            discordUser.addSocialMediaAccount(platform, socialMediaUrl)
        } catch (e: IllegalStateException) {
            event.reply("Das hinzufügen ist fehlgeschlagen. Bitte gebe eine valide URL für diese Plattform an")
                .setEphemeral(true).queue()

            return
        }
        discordUserRepository.save(discordUser)
        event.reply("Dein Social Media Account wurde erfolgreich hinzugefügt").setEphemeral(true).queue()
    }

    override fun onAutoCompleteEvent(option: String, event: CommandAutoCompleteInteractionEvent) {
        if (option == "social-media-platform") {
            val input = event.focusedOption.value.lowercase()
            event.replyStringChoices(SocialMediaPlatform.allAvailable().filter { it.platform.platformName.lowercase().contains(input) }.associate {
                it.platform.platformName to it.platform.platformName
            }).queue()
        }
    }
}