package data.database.planData;

import lombok.*;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

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

    public void planAccepted(long userId){
        invitees.get(userId).setStatus(1);
    }

    public void planDeclined(long userId){
        invitees.get(userId).setStatus(-1);
    }

    public void planDropOut(long userId){
        invitees.get(userId).setStatus(0);
    }

    public boolean isFull(){
        return getAccepted().size() >= count;
    }

    public LinkedList<Long> getAccepted(){
        LinkedList<Long> accepted = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getStatus() == 1) accepted.add(id);
        });
        return accepted;
    }

    public LinkedList<Long> getDeclined(){
        LinkedList<Long> declined = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getStatus() == -1) declined.add(id);
        });
        return declined;
    }

    public void addToLog(String message){
        SimpleDateFormat dtf = new SimpleDateFormat("MM-dd HH:mm");
        if(log == null) log = "";
        log += dtf.format(new Date())+ ": " + message + "\n";
        if(log.split("\n").length > 4){

        }
    }

    public LinkedList<Long> getPending(){
        LinkedList<Long> pending = new LinkedList<>();
        invitees.forEach((id, user) -> {
            if(user.getStatus() == 0) pending.add(id);
        });
        return pending;
    }
}
