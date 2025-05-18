package com.zgamelogic.data.database.planData.user;

import com.zgamelogic.data.database.planData.plan.Plan;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

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
        DECLINED; // -1

        public static List<Status> getHierarchalStatus(Status status){
            return switch (status){
                case DECIDING -> List.of(DECIDING, ACCEPTED, MAYBED, WAITLISTED, FILLINED);
                case ACCEPTED, WAITLISTED, FILLINED -> List.of(ACCEPTED, WAITLISTED, FILLINED);
                case MAYBED -> List.of(ACCEPTED, MAYBED, WAITLISTED, FILLINED);
                case DECLINED -> List.of(DECLINED);
            };
        }
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
    @EqualsAndHashCode
    public static class PlanUserId {
        private long planId;
        private long userId;
    }
}
