package com.zgamelogic.data.database.planData.plan;

import com.zgamelogic.data.database.planData.user.PlanUser;
import com.zgamelogic.data.plan.PlanCreationData;
import lombok.*;

import jakarta.persistence.*;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Plans")
public class Plan {
    @Id
    @Generated
    private long id;
    private String title;
    private String notes;
    private Date date;
    @Column(columnDefinition = "varchar(max)")
    private String log;
    private Long authorId;
    private Long messageId;
    private Long privateMessageId;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "id.userId")
    private Map<Long, PlanUser> invitees;

    public Plan(PlanCreationData planCreationData) {
        title = planCreationData.title();
        notes = planCreationData.notes();
        date = planCreationData.date();
        log = "";
        authorId = planCreationData.author();

        // TODO post plan to discord to get messageId
        // TODO post plan to PM to author to get privateMessageId

        invitees = new HashMap<>();
        planCreationData.players().forEach(p -> invitees.put(p, new PlanUser(this, p)));
    }
}
