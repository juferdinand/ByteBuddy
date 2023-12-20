package de.gaming.discord.bytebuddy.commands.user.data

public enum class Platform(val platformName: String){
    SPOTIFY_PLAYLIST("Spotify"),
    TWITTER("Twitter"),
    TWITCH("Twitch"),
    YOUTUBE("YouTube"),
    INSTAGRAM("Instagram"),
    TIKTOK("TikTok");

    companion object {
        fun fromPlatformName(name: String): Platform {
            return entries.find { it.platformName == name } ?: throw IllegalArgumentException("Platform $name not found")
        }
    }
}