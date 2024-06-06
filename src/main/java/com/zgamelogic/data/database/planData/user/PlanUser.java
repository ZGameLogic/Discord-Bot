package com.zgamelogic.data.database.planData.user;

import com.zgamelogic.data.database.planData.plan.Plan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "plan_user_status")
public class PlanUser {
    public enum Status {
        DECIDING, // 0
        ACCEPTED, // 1
        MAYBED, // 3
        WAITLISTED, // 2
        FILLINED, // 4
        DECLINED // -1
    }

    @EmbeddedId
    private PlanUserId id;
    @Enumerated(EnumType.STRING)
    private Status userStatus;
    private Long discordNotificationId;
    private String iosNotificationId;
    private String androidNotificationId;
    private Date waitlist_time;
    private boolean needFillIn;

    @ManyToOne
    @MapsId("planId")
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    public PlanUser(Plan plan, long userId){
        needFillIn = false;
        this.plan = plan;
        userStatus = Status.DECIDING;
        waitlist_time = null;
        id = new PlanUserId(plan.getId(), userId);
    }

    public PlanUser(Plan plan, long userId, Status status){
        needFillIn = false;
        this.plan = plan;
        userStatus = status;
        waitlist_time = null;
        id = new PlanUserId(plan.getId(), userId);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Embeddable
    public static class PlanUserId {
        private long planId;
        private long userId;
    }
}
