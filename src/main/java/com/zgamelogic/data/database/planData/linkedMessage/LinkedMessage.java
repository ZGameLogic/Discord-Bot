package com.zgamelogic.data.database.planData.linkedMessage;

import com.zgamelogic.data.database.planData.plan.Plan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "plan_linked_messages")
public class LinkedMessage {
    @EmbeddedId
    private LinkedMessageId id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    public LinkedMessage(Plan plan, long channelId, long messageId) {
        this.id = new LinkedMessageId(channelId, messageId);
        this.plan = plan;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Embeddable
    public static class LinkedMessageId {
        private long channelId;
        private long messageId;
    }
}
