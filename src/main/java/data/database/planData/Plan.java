package data.database.planData;

import lombok.*;
import net.dv8tion.jda.api.entities.Member;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

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

    public void planWaitlist(long userId) {
        User user = invitees.get(userId);
        user.setUserStatus(WAITLISTED);
        user.setWaitlist_time(new Date());
    }

    public void planAccepted(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(ACCEPTED);
        user.setWaitlist_time(null);
    }

    public void planMaybed(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(MAYBED);
        user.setWaitlist_time(null);
    }

    public void planRequestFillIned(long userId){
        User user = invitees.get(userId);
        user.setNeedFillIn(true);
        user.setWaitlist_time(null);
    }

    public void planFilledIn(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(FILLINED);
    }

    public void planDeclined(long userId){
        User user = invitees.get(userId);
        user.setUserStatus(DECLINED);
        user.setWaitlist_time(null);
    }

    public void planDropOut(long userId){
        invitees.get(userId).setUserStatus(DECIDING);
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

    public void addToLog(String message){
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
