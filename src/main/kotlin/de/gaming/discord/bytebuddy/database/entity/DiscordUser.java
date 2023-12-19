package de.gaming.discord.bytebuddy.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

}
