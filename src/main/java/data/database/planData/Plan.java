package data.database.planData;

import lombok.*;

import javax.persistence.*;
import java.text.SimpleDateFormat;
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
    @Column(name = "id")
    private long id;

    @ElementCollection
    @MapKeyColumn(name="Plan_Player_Status_Id")
    @Column(name="status")
    @CollectionTable(name="Plan_Player_Status", joinColumns=@JoinColumn(name="example_id"))
    private Map<Long, User> invitees;

    private String title;
    private String notes;
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

    public void duplicatePlanWithWaitlistFromPlan(Plan plan, User user){
        title = plan.getTitle();
        notes = plan.getNotes();
        count = plan.getCount();
        authorId = user.getId();
        channelId = plan.getChannelId();
        guildId = plan.getGuildId();
        LinkedList<Long> waitlist = plan.getWaitlist();
        waitlist.remove(user.getId());
        invitees = new HashMap<>();
        for(int i = 0; i < count; i++){
            User cUser = plan.getInvitees().get(waitlist.removeFirst());
            plan.planDeclined(cUser.getId());
            invitees.put(cUser.getId(), cUser);
            planAccepted(cUser.getId());
        }
        plan.addToLog("User duplicated this plan");
        addToLog("Plan created with duplication");
    }

    public void planWaitlist(long userId) {
        User user = invitees.get(userId);
        user.setStatus(User.Status.Waitlist);
        user.setWaitlist_time(new Date());
    }

    public void planAccepted(long userId){
        User user = invitees.get(userId);
        user.setStatus(User.Status.Accepted);
        user.setWaitlist_time(null);
    }

    public void planDeclined(long userId){
        User user = invitees.get(userId);
        user.setStatus(User.Status.Declined);
        user.setWaitlist_time(null);
    }

    public void planMaybe(long userId){
        User user = invitees.get(userId);
        user.setStatus(User.Status.Maybe);
        user.setWaitlist_time(null);
    }

    public void planDropOut(long userId){
        User user = invitees.get(userId);
        user.setStatus(User.Status.Waiting);
        user.setWaitlist_time(null);
    }

    public boolean isFull(){
        return getAccepted().size() >= count;
    }

    public LinkedList<Long> getAccepted(){
        LinkedList<Long> accepted = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getStatus() == User.Status.Accepted) accepted.add(id);
        });
        return accepted;
    }

    public LinkedList<Long> getDeclined(){
        LinkedList<Long> declined = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getStatus() == User.Status.Declined) declined.add(id);
        });
        return declined;
    }

    public LinkedList<Long> getWaitlist(){
        LinkedList<User> waitlist = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getStatus() == User.Status.Waitlist) waitlist.add(user);
        });
        Comparator<User> dateComparator = Comparator.comparing(User::getWaitlist_time);
        waitlist.sort(dateComparator);
        LinkedList<Long> sortedIds =  new LinkedList<>();
        waitlist.forEach(user -> {
            sortedIds.add(user.getId());
        });
        return sortedIds;
    }

    public LinkedList<Long> getMaybe(){
        LinkedList<Long> maybe = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getStatus() == User.Status.Maybe) maybe.add(id);
        });
        return maybe;
    }

    public void addToLog(String message){
        SimpleDateFormat dtf = new SimpleDateFormat("MM-dd HH:mm");
        if(log == null) log = "";
        log += dtf.format(new Date())+ ": " + message + "\n";
    }

    public LinkedList<Long> getPending(){
        LinkedList<Long> pending = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getStatus() == User.Status.Waiting) pending.add(id);
        });
        return pending;
    }
}
