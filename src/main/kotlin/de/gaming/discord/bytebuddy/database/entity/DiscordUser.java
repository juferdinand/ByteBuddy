package de.gaming.discord.bytebuddy.database.entity;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Set;

import de.gaming.discord.bytebuddy.commands.user.data.Platform;
import de.gaming.discord.bytebuddy.commands.user.data.SocialMediaPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DiscordUser {

    @Id
    @Column(name = "discord_user_id", nullable = false, unique = true)
    private Long discordUserId;

    @Column(name = "discord_user_name", nullable = false)
    private String discordUserName;

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @ManyToMany(targetEntity = Game.class)
    @JoinTable(name = "discord_user_games", joinColumns = @JoinColumn(name = "discord_user_id"), inverseJoinColumns = @JoinColumn(name = "game_id"))
    private Set<Game> games;

    @ManyToMany(targetEntity = Game.class)
    @JoinTable(name = "discord_user_favorite_games", joinColumns = @JoinColumn(name = "discord_user_id"), inverseJoinColumns = @JoinColumn(name = "game_id"))
    private Set<Game> favoriteGames;

    @Column(name = "favorite_color")
    private String favoriteColor;

    @Column(name = "spotify_playlist_url")
    private String spotifyPlaylistUrl;

    @Column(name = "youtube_channel_url")
    private String youtubeChannelUrl;

    @Column(name = "twitch_channel_url")
    private String twitchChannelUrl;

    @Column(name = "twitter_channel_url")
    private String twitterChannelUrl;

    @Column(name = "instagram_channel_url")
    private String instagramChannelUrl;

    @Column(name = "tiktok_channel_url")
    private String tiktokChannelUrl;


    public void addSocialMediaAccount(
            @NotNull Platform platform,
            @NotNull String socialMediaUrl) {
        switch (platform) {
            case SPOTIFY_PLAYLIST:
                if (!socialMediaUrl.contains("https://open.spotify.com/playlist/"))
                    throw new IllegalStateException("Unexpected value: " + socialMediaUrl + " is not a valid spotify playlist url!");

                this.spotifyPlaylistUrl = socialMediaUrl;
                break;
            case YOUTUBE:
                if (!socialMediaUrl.contains("https://www.youtube.com/channel/") && !socialMediaUrl.contains("https://www.youtube.com/user/"))
                    throw new IllegalStateException("Unexpected value: " + socialMediaUrl + " is not a valid youtube channel url!");
                this.youtubeChannelUrl = socialMediaUrl;
                break;
            case TWITCH:
                if (!socialMediaUrl.contains("https://www.twitch.tv/"))
                    throw new IllegalStateException("Unexpected value: " + socialMediaUrl + " is not a valid twitch channel url!");
                this.twitchChannelUrl = socialMediaUrl;
                break;
            case TWITTER:
                if (!socialMediaUrl.contains("https://twitter.com/"))
                    throw new IllegalStateException("Unexpected value: " + socialMediaUrl + " is not a valid twitter channel url!");
                this.twitterChannelUrl = socialMediaUrl;
                break;
            case INSTAGRAM:
                if (!socialMediaUrl.contains("https://www.instagram.com/"))
                    throw new IllegalStateException("Unexpected value: " + socialMediaUrl + " is not a valid instagram channel url!");
                this.instagramChannelUrl = socialMediaUrl;
                break;
            case TIKTOK:
                if (!socialMediaUrl.contains("https://www.tiktok.com/@"))
                    throw new IllegalStateException("Unexpected value: " + socialMediaUrl + " is not a valid tiktok channel url!");
                this.tiktokChannelUrl = socialMediaUrl;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + platform);
        }
    }

    public void addGame(@NotNull Game game) {
        this.games.add(game);
    }
}
