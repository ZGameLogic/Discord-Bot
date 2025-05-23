package com.zgamelogic.data.database.planData.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zgamelogic.data.database.planData.linkedMessage.LinkedMessage;
import com.zgamelogic.data.database.planData.user.PlanUser;
import com.zgamelogic.data.intermediates.planData.PlanEvent;
import com.zgamelogic.data.plan.PlanCreationData;
import lombok.*;

import jakarta.persistence.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.zgamelogic.data.database.planData.user.PlanUser.Status.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Plans")
@JsonSerialize(using = Plan.PlanSerialization.class)
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
    private Boolean deleted;
    private Long pollId;
    private Long pollChannelId;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "id.userId")
    private Map<Long, PlanUser> invitees;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LinkedMessage> linked;

    public Plan(PlanCreationData planCreationData) {
        title = planCreationData.title();
        notes = planCreationData.notes();
        date = planCreationData.date();
        log = "";
        authorId = planCreationData.author();
        count = planCreationData.count();
        invitees = new HashMap<>();
        linked =   new LinkedList<>();
        deleted = false;
        pollId = planCreationData.pollId();
        pollChannelId = planCreationData.pollChannelId();
    }

    public boolean isFull(){
        if(count == -1) return false;
        return getAcceptedIds().size() >= count;
    }
    public boolean isNeedFillIn(){ return invitees.values().stream().anyMatch(PlanUser::isNeedFillIn); }
    public List<Long> getAcceptedIds(){ return getUserIdsWithStatus(ACCEPTED); }
    public List<Long> getAcceptedIdsAndAuthor(){
        List<Long> users = getUserIdsWithStatus(ACCEPTED);
        users.add(authorId);
        return users;
    }
    public List<Long> getAllUsers(){
        List<Long> users = new ArrayList<>();
        users.add(authorId);
        users.addAll(invitees.keySet());
        return users;
    }
    public List<Long> getDeclinedIds(){ return getUserIdsWithStatus(PlanUser.Status.DECLINED); }
    public List<Long> getWaitlistIds(){ return getUserIdsWithStatus(WAITLISTED); }
    public List<Long> getMaybesIds(){ return getUserIdsWithStatus(PlanUser.Status.MAYBED); }
    public List<Long> getPendingIds(){ return getUserIdsWithStatus(DECIDING); }
    public List<Long> getFillinedListIds(){ return getUserIdsWithStatus(PlanUser.Status.FILLINED); }
    public List<PlanUser> getAccepted(){ return getUserWithStatus(ACCEPTED); }
    public List<PlanUser> getDeclined(){ return getUserWithStatus(PlanUser.Status.DECLINED); }
    public List<PlanUser> getWaitlist(){ return getUserWithStatus(WAITLISTED); }
    public List<PlanUser> getMaybes(){ return getUserWithStatus(PlanUser.Status.MAYBED); }
    public List<PlanUser> getPending(){ return getUserWithStatus(DECIDING); }
    public List<PlanUser> getFillinedList(){ return getUserWithStatus(PlanUser.Status.FILLINED); }

    private List<Long> getUserIdsWithStatus(PlanUser.Status status) {
        return invitees.values().stream()
                .filter(user -> user.getUserStatus() == status)
                .map(user -> user.getId().getUserId())
                .collect(Collectors.toList());
    }

    private List<PlanUser> getUserWithStatus(PlanUser.Status status) {
        return invitees.values().stream()
                .filter(user -> user.getUserStatus() == status)
                .toList();
    }

    public List<PlanUser> getUserWithStatus(List<PlanUser.Status> status) {
        return invitees.values().stream()
                .filter(user -> status.contains(user.getUserStatus()))
                .toList();
    }

    public boolean requestedFillIn(long id){
        return invitees.values().stream()
                .anyMatch(user -> user.getId().getUserId() == id && user.isNeedFillIn());
    }

    @JsonIgnore
    public List<Long> getNotDeclinedIds(){
        List<Long> ids = new ArrayList<>(invitees.values().stream()
                .filter(invitee -> invitee.getUserStatus() != DECLINED)
                .map(invitee -> invitee.getId().getUserId()).toList()
        );
        ids.add(authorId);
        return ids;
    }

    public void addLinkedMessage(long channelId, long messageId){
        linked.add(new LinkedMessage(this, channelId, messageId));
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
        List<Long> fillIns = getFillinedListIds();
        while(!fillIns.isEmpty() && !isFull()){
            long uid = fillIns.remove(0);
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_FILLIN_TO_ACCEPTED, uid));
            planAccepted(uid);
        }

        // Check to see if event isn't full and people are in wait list
        List<Long> waitlists = getWaitlistIds();
        while(!waitlists.isEmpty() && !isFull()){
            long uid = waitlists.remove(0);
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_WAITLIST_TO_ACCEPTED, uid));
            planAccepted(uid);
        }

        // check to see if event is full and a fill in is request and people are in the waitlist
        waitlists = getWaitlistIds();
        while(!waitlists.isEmpty() && isNeedFillIn()){
            long uid = waitlists.remove(0);
            subsequentEvents.add(new PlanEvent(PlanEvent.Event.USER_MOVED_WAITLIST_TO_FILL_IN, uid));
            planFilledIn(uid);
        }

        // check to see if the event has enough people wait listed to make another event
        if(getWaitlistIds().size() >= count + 1 && count != -1){
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

    public static class PlanSerialization extends JsonSerializer<Plan> {

        @Override
        public void serialize(Plan value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("id", value.getId());
            gen.writeStringField("title", value.getTitle());
            gen.writeStringField("notes", value.getNotes());
            if(value.getDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String formattedDate = dateFormat.format(value.getDate());
                gen.writeStringField("start time", formattedDate);
            }
            gen.writeNumberField("count", value.getCount());
            gen.writeNumberField("author id", value.getAuthorId());
            gen.writeArrayFieldStart("invitees");
            value.getInvitees().values().forEach(planUser -> {
                try {
                    gen.writeStartObject();
                    gen.writeNumberField("user id", planUser.getId().getUserId());
                    gen.writeStringField("status", planUser.getUserStatus().name());
                    gen.writeBooleanField("needs fill in", planUser.isNeedFillIn());
                    gen.writeEndObject();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }
}
