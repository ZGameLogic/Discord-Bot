package com.zgamelogic.data.database.planData.linkedMessage;

import com.zgamelogic.data.database.planData.plan.Plan;
import jakarta.persistence.*;
import lombok.*;

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
    @EqualsAndHashCode
    public static class LinkedMessageId {
        private long channelId;
        private long messageId;
    }
}
