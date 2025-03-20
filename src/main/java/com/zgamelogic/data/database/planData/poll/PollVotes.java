package com.zgamelogic.data.database.planData.poll;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "user_poll_votes")
@IdClass(PollVotes.PollVotesId.class)
public class PollVotes {
    @Id
    private long pollId;
    @Id
    private long optionId;
    @Id
    private long userId;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class PollVotesId {
        private long pollId;
        private long optionId;
        private long userId;
    }
}
