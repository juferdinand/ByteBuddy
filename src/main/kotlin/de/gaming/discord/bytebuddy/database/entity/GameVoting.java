package de.gaming.discord.bytebuddy.database.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GameVoting {

    public GameVoting(Game gameId, Voting votingId, DiscordUser discordUserId) {
        this.gameVotingVote = true;
        this.gameId = gameId;
        this.votingId = votingId;
        this.discordUserId = discordUserId;
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String gameVotingId;

    @Column(name = "game_voting_vote", nullable = false)
    private Boolean gameVotingVote;

    @Access(AccessType.PROPERTY)
    @ManyToOne(targetEntity = Game.class)
    @JoinColumn(name = "game_id", referencedColumnName = "game_id")
    private Game gameId;

    @Access(AccessType.PROPERTY)
    @ManyToOne(targetEntity = Voting.class)
    @JoinColumn(name = "voting_id", referencedColumnName = "voting_id")
    private Voting votingId;

    @Access(AccessType.PROPERTY)
    @ManyToOne(targetEntity = DiscordUser.class)
    @JoinColumn(name = "discord_user_id", referencedColumnName = "discord_user_id")
    private DiscordUser discordUserId;

}
