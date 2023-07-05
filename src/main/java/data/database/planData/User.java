package data.database.planData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class User {

    public enum Status {
        DECIDING, // 0
        ACCEPTED, // 1
        MAYBED, // 3
        WAITLISTED, // 2
        FILLINED, // 4
        DECLINED // -1
    }

    private Long id;
    @Enumerated(EnumType.STRING)
    private Status userStatus;
    private Long messageId;
    private Date waitlist_time;
    private Boolean needFillIn;

    public User(Long id, Status userStatus) {
        this.id = id;
        this.userStatus = userStatus;
    }
}
