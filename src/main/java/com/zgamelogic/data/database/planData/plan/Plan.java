package com.zgamelogic.data.database.planData.plan;

import com.zgamelogic.data.database.planData.user.PlanUser;
import com.zgamelogic.data.intermediates.planData.PlanEvent;
import com.zgamelogic.data.plan.PlanCreationData;
import lombok.*;

import jakarta.persistence.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.zgamelogic.data.database.planData.user.PlanUser.Status.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Plans")
public class Plan {
    @Id
    @GeneratedValue
    private long id;
    private String title;
    private String notes;
    private Date date;
    @Column(columnDefinition = "varchar(max)")
    private String log;
    private long authorId;
    private Long messageId;
    private Long privateMessageId;
    private int count;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "id.userId")
    private Map<Long, PlanUser> invitees;

    public Plan(PlanCreationData planCreationData) {
        title = planCreationData.title();
        notes = planCreationData.notes();
        date = planCreationData.date();
        log = "";
        authorId = planCreationData.author();
        count = planCreationData.count();
        invitees = new HashMap<>();
    }

    public boolean isFull(){ return getAccepted().size() >= count; }
    public boolean isNeedFillIn(){ return invitees.values().stream().anyMatch(PlanUser::isNeedFillIn); }
    public List<Long> getAccepted(){ return getUsersWithStatus(ACCEPTED); }
    public List<Long> getDeclined(){ return getUsersWithStatus(PlanUser.Status.DECLINED); }
    public List<Long> getWaitlist(){ return getUsersWithStatus(WAITLISTED); }
    public List<Long> getMaybes(){ return getUsersWithStatus(PlanUser.Status.MAYBED); }
    public List<Long> getPending(){ return getUsersWithStatus(DECIDING); }
    public List<Long> getFillInedList(){ return getUsersWithStatus(PlanUser.Status.FILLINED); }

    private List<Long> getUsersWithStatus(PlanUser.Status status) {
        return invitees.values().stream()
                .filter(user -> user.getUserStatus() == status)
                .map(user -> user.getId().getUserId())
                .toList();
    }

    public boolean requestedFillIn(long id){
        return invitees.values().stream()
                .anyMatch(user -> user.getId().getUserId() == id && user.isNeedFillIn());
    }

    public LinkedList<PlanEvent> processEvents(PlanEvent...events){
        LinkedList<PlanEvent> subsequentEvents = new LinkedList<>(Arrays.asList(events));
        // process predicate events
        for(PlanEvent event: events){
            long uid = event.getUid();
            switch (event.getEvent()){
                case USER_ACCEPTED:
                    planAccepted(uid);
                    break;
                case USER_MAYBED:
                    planMaybed(uid);
                    break;
                case USER_WAITLISTED:
                    planWaitlist(uid);
                    break;
                case USER_REGISTERED_FOR_FILL_IN:
                    planRequestFillIned(uid);
                    break;
                case USER_FILLINED:
                    planFilledIn(uid);
                    break;
                case USER_DECLINED:
                    planDeclined(uid);
                    break;
                case USER_DROPPED_OUT:
                    planDroppedOut(uid);
                    break;
            }
        }
        // Check to see if event isn't full and people are in fill-in list
        List<Long> fillIns = getFillInedList();
        while(!fillIns.isEmpty() && !isFull()){
            long uid = fillIns.remove(0);
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_FILLIN_TO_ACCEPTED, uid));
            planAccepted(uid);
        }

        // Check to see if event isn't full and people are in wait list
        List<Long> waitlists = getWaitlist();
        while(!waitlists.isEmpty() && !isFull()){
            long uid = waitlists.remove(0);
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_WAITLIST_TO_ACCEPTED, uid));
            planAccepted(uid);
        }

        // check to see if event is full and a fill in is request and people are in the waitlist
        waitlists = getWaitlist();
        while(!waitlists.isEmpty() && isNeedFillIn()){
            long uid = waitlists.remove(0);
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_WAITLIST_TO_FILL_IN, uid));
            planFilledIn(uid);
        }

        // check to see if the event has enough people wait listed to make another event
        if(getWaitlist().size() >= count + 1 && count != -1){
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.EVENT_CREATED_FROM_WAITLIST, 0L));
        }

        return subsequentEvents;
    }

    private void planWaitlist(long userId) {
        PlanUser user = invitees.get(userId);
        user.setUserStatus(WAITLISTED);
        user.setWaitlist_time(new Date());
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> wait-listed the event");
    }

    private void planAccepted(long userId){
        PlanUser user = invitees.get(userId);
        user.setUserStatus(ACCEPTED);
        user.setWaitlist_time(null);
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> accepted the event");
    }

    private void planDroppedOut(long userId){
        PlanUser user = invitees.get(userId);
        user.setUserStatus(DECIDING);
        user.setWaitlist_time(null);
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> Dropped out of the event");
    }

    private void planMaybed(long userId){
        PlanUser user = invitees.get(userId);
        user.setUserStatus(MAYBED);
        user.setWaitlist_time(null);
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> maybed the event");
    }

    private void planRequestFillIned(long userId){
        PlanUser user = invitees.get(userId);
        user.setWaitlist_time(null);
        user.setNeedFillIn(true);
        addToLog("<@"+ userId+ "> requested a fill in for the event");
    }

    private void planFilledIn(long userId){
        PlanUser user = invitees.get(userId);
        user.setUserStatus(FILLINED);
        user.setWaitlist_time(new Date());
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> filled in for the event");
    }

    private void planDeclined(long userId){
        PlanUser user = invitees.get(userId);
        user.setUserStatus(DECLINED);
        user.setWaitlist_time(null);
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> declined the event");
    }

    private void addToLog(String message){
        SimpleDateFormat dtf = new SimpleDateFormat("MM-dd HH:mm");
        if(log == null) log = "";
        log += dtf.format(new Date())+ ": " + message + "\n";
    }
}
