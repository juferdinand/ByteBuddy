package de.gaming.discord.bytebuddy.commands.user.data

import lombok.Getter

@Getter
class SocialMediaPlatform(val emoji: String, val platform: Platform) {
    companion object {

        fun allAvailable(): List<SocialMediaPlatform> {
            return this.allAvailable
        }

        private val allAvailable = listOf(
            SocialMediaPlatform("<:spotify:1187032911636086814>", Platform.SPOTIFY_PLAYLIST),
            SocialMediaPlatform("<:twitter:1187032947048587306>", Platform.TWITTER),
            SocialMediaPlatform("<:twitch:1187032870502535301>", Platform.TWITCH),
            SocialMediaPlatform("<:youtube:1187032751363334206>", Platform.YOUTUBE),
            SocialMediaPlatform("<:instagram:1187032809949384746>", Platform.INSTAGRAM),
            SocialMediaPlatform("<:tiktok:1187032841121435762>", Platform.TIKTOK)
        )

        private val testServerAllAvailable = listOf(
            SocialMediaPlatform("<:spotify:1187006256121135184>", Platform.SPOTIFY_PLAYLIST),
            SocialMediaPlatform("<:twitter:1187005980333068438>", Platform.TWITTER),
            SocialMediaPlatform("<:twitch:1187006334827245648>", Platform.TWITCH),
            SocialMediaPlatform("<:youtube:1187006460098531348>", Platform.YOUTUBE),
            SocialMediaPlatform("<:instagram:1187006114395586581>", Platform.INSTAGRAM),
            SocialMediaPlatform("<:tiktok:1187006303290265661>", Platform.TIKTOK)
        )
    }
}