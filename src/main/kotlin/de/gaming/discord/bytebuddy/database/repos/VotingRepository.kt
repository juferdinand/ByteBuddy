package de.gaming.discord.bytebuddy.database.repos

import de.gaming.discord.bytebuddy.database.entity.Voting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface VotingRepository : JpaRepository<Voting, Int>{

    @Query("SELECT v FROM Voting v JOIN FETCH v.gamesOfVoting JOIN FETCH v.createdBy WHERE v.votingId = :votingId")
    fun findVotingByVotingId(votingId: Int): Voting?

    @Query("SELECT v FROM Voting v JOIN FETCH v.gamesOfVoting JOIN FETCH v.createdBy WHERE v.votingId = :votingId")
    fun findVotingByVotingId(votingId: String): Voting?

    @Query("SELECT v FROM Voting v JOIN FETCH v.gamesOfVoting JOIN FETCH v.createdBy WHERE v.votingTime > CURRENT_TIMESTAMP")
    fun findAllByVotingTimeIsNotInPast(): List<Voting>
    @Query("SELECT v FROM Voting v JOIN FETCH v.gamesOfVoting JOIN FETCH v.createdBy WHERE v.votingTime < CURRENT_TIMESTAMP AND v.isCompleted = false")
    fun findAllByVotingTimeIsInPastAndIsCompletedFalse(): List<Voting>
}