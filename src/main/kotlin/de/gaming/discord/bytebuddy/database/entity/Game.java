package de.gaming.discord.bytebuddy.database.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id", nullable = false, unique = true)
    private Integer gameId;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "game_reason", nullable = false)
    private String gameReason;

    @Column(name = "game_url", nullable = false)
    private String gameUrl;

    @Column(name = "game_image", nullable = false)
    private String gameImage;

    @Column(name = "game_price", nullable = false)
    private BigDecimal gamePrice;

    @Column(name = "game_bought", nullable = false)
    private Boolean gameBought = false;

    @Access(AccessType.PROPERTY)
    @ManyToOne(targetEntity = DiscordUser.class)
    @JoinColumn(name = "created_by", referencedColumnName = "discord_user_id")
    private DiscordUser createdBy;

    @Column(name = "created_date")
    private Instant createdDate = Instant.now();

    @Column(name = "game_release_date")
    private String gameReleaseDateText;
}
