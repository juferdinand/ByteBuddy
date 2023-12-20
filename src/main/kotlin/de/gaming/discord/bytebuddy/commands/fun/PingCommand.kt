package de.gaming.discord.bytebuddy.commands.`fun`

import io.viascom.discord.bot.aluna.bot.DiscordCommand
import io.viascom.discord.bot.aluna.bot.Interaction
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Interaction
class PingCommand : DiscordCommand("ping", "Send a ping") {
    override fun execute(event: SlashCommandInteractionEvent) {
        event.reply("Pong\nYour locale is:${this.userLocale}").queue()
    }
}