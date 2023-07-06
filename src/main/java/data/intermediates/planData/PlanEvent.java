package data.intermediates.planData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class PlanEvent {
    public enum Event {
        USER_ACCEPTED,
        USER_MAYBED,
        USER_WAITLISTED,
        USER_REGISTERED_FOR_FILL_IN,
        USER_FILLINED,
        USER_DECLINED,
        USER_DROPPED_OUT,
        USER_MOVED_FILLIN_TO_ACCEPTED,
        USER_MOVED_WAITLIST_TO_ACCEPTED,
        USER_MOVED_WAITLIST_TO_FILL_IN,
        EVENT_CREATED_FROM_WAITLIST
    }

    private Event event;
    private Long uid;
}