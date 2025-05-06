package com.zgamelogic.data.database.planData.reminder;

import com.zgamelogic.data.database.planData.plan.Plan;
import com.zgamelogic.data.database.planData.user.PlanUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "plan_reminder")
public class PlanReminder {
    @EmbeddedId
    private PlanReminderId id;
    @Enumerated(EnumType.STRING)
    private PlanUser.Status minStatus;
    private String message;

    @ManyToOne
    @MapsId("planId")
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    public PlanReminder(Plan plan, Date time, PlanUser.Status minStatus, String message){
        this.plan = plan;
        id = new PlanReminderId(plan.getId(), time);
        this.minStatus = minStatus;
        this.message = message;
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PlanReminderId {
        private long planId;
        private Date time;
    }
}
