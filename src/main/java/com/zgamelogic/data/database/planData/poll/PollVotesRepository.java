package com.zgamelogic.data.database.planData.poll;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollVotesRepository extends JpaRepository<PollVotes, PollVotes.PollVotesId> {
    List<PollVotes> findAllByPollIdAndOptionId(long pollId, long optionId);
}
