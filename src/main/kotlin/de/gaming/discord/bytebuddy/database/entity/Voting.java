package de.gaming.discord.bytebuddy.database.entity;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Voting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voting_id", nullable = false, unique = true)
    private Integer votingId;

    @Column(name = "voting_name", nullable = false)
    private String votingName;

    @Column(name = "target_channel_id", nullable = false)
    private Long targetChannelId;

    @Column(name = "voting_message_id")
    private Long votingMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "voting_reason", nullable = false)
    private VotingReason votingReason;

    @Column(name = "voting_active_until", nullable = false)
    private Date votingTime;

    @Access(AccessType.PROPERTY)
    @ManyToOne(targetEntity = DiscordUser.class)
    @JoinColumn(name = "created_by", referencedColumnName = "discord_user_id")
    private DiscordUser createdBy;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @ManyToMany(targetEntity = Game.class)
    @JoinTable(name = "games_of_voting", joinColumns = @JoinColumn(name = "voting_id"), inverseJoinColumns = @JoinColumn(name = "game_id"))
    private Set<Game> gamesOfVoting = new HashSet<>();

    @Column(name = "is_completed")
    private boolean isCompleted = false;
}
