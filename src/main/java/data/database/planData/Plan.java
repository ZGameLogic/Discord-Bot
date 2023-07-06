package data.database.planData;

import data.intermediates.planData.PlanEvent;
import lombok.*;
import net.dv8tion.jda.api.entities.Member;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static data.database.planData.User.Status.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Plans")
public class Plan {
    @Id
    @Column(name = "id")
    private long id;

    @ElementCollection
    @MapKeyColumn(name="Plan_Player_Status_Id")
    @Column(name="status")
    @CollectionTable(name="Plan_Player_Status", joinColumns=@JoinColumn(name="example_id"))
    private Map<Long, User> invitees;

    private String title;
    private String notes;
    private Date date;
    @Column(columnDefinition = "varchar(max)")
    private String log;
    private Long authorId;
    private Long guildId;
    private Long channelId;
    private Long messageId;
    private Long privateMessageId;
    private Integer count;

    public void updateMessageIdForUser(long userId, long messageId){
        invitees.get(userId).setMessageId(messageId);
    }

    public void addUser(Member member){
        invitees.put(member.getIdLong(), new User(member.getIdLong(), DECIDING));
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
        LinkedList<Long> fillIns = getFillInedList();
        while(fillIns.size() > 0 && !isFull()){
            long uid = fillIns.removeFirst();
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_FILLIN_TO_ACCEPTED, uid));
            planAccepted(uid);
        }

        // Check to see if event isn't full and people are in wait list
        LinkedList<Long> waitlists = getWaitlist();
        while(waitlists.size() > 0 && !isFull()){
            long uid = waitlists.removeFirst();
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_WAITLIST_TO_ACCEPTED, uid));
            planAccepted(uid);
        }

        // check to see if event is full and a fill in is request and people are in the waitlist
        waitlists = getWaitlist();
        while(waitlists.size() > 0 && isNeedFillIn()){
            long uid = waitlists.removeFirst();
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_WAITLIST_TO_FILL_IN, uid));
            planFilledIn(uid);
        }

        // check to see if the event has enough people wait listed to make another event
        if(getWaitlist().size() >= count + 1 && count != -1){
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.EVENT_CREATED_FROM_WAITLIST, 0L));
        }

        return subsequentEvents;
    }

    public Plan createPlanFromWaitlist(){
        Plan newPlan = new Plan();
        newPlan.setCount(count);
        newPlan.setTitle(title);
        newPlan.setNotes(notes + "\nPlan created from previous plan due to large waitlist.");
        newPlan.setDate(date);
        newPlan.setGuildId(guildId);
        newPlan.setChannelId(channelId);
        newPlan.addToLog("Plan created from previous plan due to large waitlist.");
        HashMap<Long, User> waitlistUsers = new HashMap<>();
        for (int i = 0; i < count + 1; i++) {
            User user = invitees.get(getWaitlist().removeFirst());
            planDeclined(user.getId());
            if(i == 0){
                newPlan.setAuthorId(user.getId());
            } else {
                waitlistUsers.put(user.getId(), new User(user.getId(), ACCEPTED));
            }
        }
        newPlan.setInvitees(waitlistUsers);

        return newPlan;
    }

    private void planWaitlist(long userId) {
        User user = invitees.get(userId);
        user.setUserStatus(WAITLISTED);
        user.setWaitlist_time(new Date());
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> wait-listed the event");
    }

    private void planAccepted(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(ACCEPTED);
        user.setWaitlist_time(null);
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> accepted the event");
    }

    private void planDroppedOut(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(DECIDING);
        user.setWaitlist_time(null);
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> Dropped out of the event");
    }

    private void planMaybed(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(MAYBED);
        user.setWaitlist_time(null);
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> maybed the event");
    }

    private void planRequestFillIned(long userId){
        User user = invitees.get(userId);
        user.setWaitlist_time(null);
        user.setNeedFillIn(true);
        addToLog("<@"+ userId+ "> requested a fill in for the event");
    }

    private void planFilledIn(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(FILLINED);
        user.setWaitlist_time(new Date());
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> filled in for the event");
    }

    private void planDeclined(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(DECLINED);
        user.setWaitlist_time(null);
        user.setNeedFillIn(false);
        addToLog("<@"+ userId+ "> declined the event");
    }

    public boolean requestedFillIn(long id){
        for(User user: invitees.values()){
            if(user.getNeedFillIn() && id == user.getId()){
                return true;
            }
        }
        return false;
    }

    public boolean isFull(){
        if(count == -1) return false;
        return getAccepted().size() >= count;
    }

    public boolean isNeedFillIn(){
        int needed = 0;
        for(User user: invitees.values()){
            if(user.getNeedFillIn()){
                needed++;
            } else if(user.getUserStatus() == FILLINED){
                needed--;
            }
        }
        return needed != 0;
    }

    public LinkedList<Long> getAccepted(){
        LinkedList<Long> accepted = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getUserStatus() == ACCEPTED) accepted.add(id);
        });
        return accepted;
    }

    public LinkedList<Long> getDeclined(){
        LinkedList<Long> declined = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getUserStatus() == DECLINED) declined.add(id);
        });
        return declined;
    }

    public LinkedList<Long> getMaybes(){
        LinkedList<Long> maybe = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getUserStatus() == MAYBED) maybe.add(id);
        });
        return maybe;
    }

    public LinkedList<Long> getWaitlist(){
        LinkedList<User> waitlist = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getUserStatus() == WAITLISTED) waitlist.add(user);
        });
        Comparator<User> dateComparator = Comparator.comparing(User::getWaitlist_time);
        waitlist.sort(dateComparator);
        LinkedList<Long> sortedIds =  new LinkedList<>();
        waitlist.forEach(user -> sortedIds.add(user.getId()));
        return sortedIds;
    }

    public LinkedList<Long> getFillInedList(){
        LinkedList<User> fillInList = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getUserStatus() == FILLINED) fillInList.add(user);
        });
        Comparator<User> dateComparator = Comparator.comparing(User::getWaitlist_time);
        fillInList.sort(dateComparator);
        LinkedList<Long> sortedIds =  new LinkedList<>();
        fillInList.forEach(user -> sortedIds.add(user.getId()));
        return sortedIds;
    }

    private void addToLog(String message){
        SimpleDateFormat dtf = new SimpleDateFormat("MM-dd HH:mm");
        if(log == null) log = "";
        log += dtf.format(new Date())+ ": " + message + "\n";
    }

    public LinkedList<Long> getPending(){
        LinkedList<Long> pending = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getUserStatus() == DECIDING) pending.add(id);
        });
        return pending;
    }
}
