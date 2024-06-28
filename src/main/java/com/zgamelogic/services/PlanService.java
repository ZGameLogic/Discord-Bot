package com.zgamelogic.services;

import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.bot.utils.PlanHelper;
import com.zgamelogic.data.database.planData.plan.Plan;
import com.zgamelogic.data.database.planData.plan.PlanRepository;
import com.zgamelogic.data.database.planData.user.PlanUser;
import com.zgamelogic.data.intermediates.planData.PlanEvent;
import com.zgamelogic.data.plan.PlanCreationData;
import com.zgamelogic.data.plan.PlanEventResultMessage;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.zgamelogic.bot.utils.PlanHelper.*;
import static com.zgamelogic.data.Constants.*;
import static com.zgamelogic.data.intermediates.planData.PlanEvent.Event.*;

@SuppressWarnings("ALL")
@Service
@DiscordController
@Slf4j
public class PlanService {

    @Value("${discord.guild}")
    private long discordGuildId;
    @Value("${discord.plan.id}")
    private long discordPlanId;
    @Value("${discord.noInviteRole.id}")
    private long discordNoInviteRoleId;

    @Bot
    private JDA bot;

    private final PlanRepository planRepository;

    private TextChannel planTextChannel;
    private Guild discordGuild;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @DiscordMapping
    private void onReady(ReadyEvent event){
        discordGuild = event.getJDA().getGuildById(discordGuildId);
        planTextChannel = discordGuild.getTextChannelById(discordPlanId);
    }

    public PlanEventResultMessage requestFillIn(long planId, long userId){
        if(!planRepository.existsById(planId)) return PlanEventResultMessage.failure(PLAN_NOT_FOUND);
        if(!isDiscordUser(userId)) return PlanEventResultMessage.failure(USER_NOT_FOUND);
        Plan plan = planRepository.getReferenceById(planId);
        PlanUser user = plan.getInvitees().get(userId);
        if(user == null) return PlanEventResultMessage.failure(USER_NOT_IN_PLAN);
        PlanEvent planEvent = new PlanEvent(USER_REGISTERED_FOR_FILL_IN, userId);
        updateEvent(plan, planEvent);
        return PlanEventResultMessage.success(PLAN_UPDATED);
    }

    public PlanEventResultMessage fillIn(long planId, long userId){
        if(!planRepository.existsById(planId)) return PlanEventResultMessage.failure(PLAN_NOT_FOUND);
        if(!isDiscordUser(userId)) return PlanEventResultMessage.failure(USER_NOT_FOUND);
        Plan plan = planRepository.getReferenceById(planId);
        PlanUser user = plan.getInvitees().get(userId);
        if(user == null) return PlanEventResultMessage.failure(USER_NOT_IN_PLAN);
        if(!plan.isNeedFillIn()) return PlanEventResultMessage.failure(NO_FILLINS_AVAILABLE);
        PlanEvent planEvent = new PlanEvent(USER_FILLINED, userId);
        updateEvent(plan, planEvent);
        return PlanEventResultMessage.success(PLAN_UPDATED);
    }

    public PlanEventResultMessage dropOut(long planId, long userId){
        if(!planRepository.existsById(planId)) return PlanEventResultMessage.failure(PLAN_NOT_FOUND);
        if(!isDiscordUser(userId)) return PlanEventResultMessage.failure(USER_NOT_FOUND);
        Plan plan = planRepository.getReferenceById(planId);
        PlanUser user = plan.getInvitees().get(userId);
        if(user == null) return PlanEventResultMessage.failure(USER_NOT_IN_PLAN);
        if(user.getUserStatus() != PlanUser.Status.ACCEPTED) return PlanEventResultMessage.failure(USER_NOT_ACCEPTED_PLAN);
        PlanEvent planEvent = new PlanEvent(USER_DROPPED_OUT, userId);
        updateEvent(plan, planEvent);
        return PlanEventResultMessage.success(PLAN_UPDATED);
    }

    public PlanEventResultMessage accept(long planId, long userId){
        if(!planRepository.existsById(planId)) return PlanEventResultMessage.failure(PLAN_NOT_FOUND);
        if(!isDiscordUser(userId)) return PlanEventResultMessage.failure(USER_NOT_FOUND);
        Plan plan = planRepository.getReferenceById(planId);
        PlanUser user = plan.getInvitees().get(userId);
        if(user == null) return PlanEventResultMessage.failure(USER_NOT_IN_PLAN);
        if(plan.getAcceptedIds().size() >= plan.getCount()) return PlanEventResultMessage.failure(PLAN_FULL);
        PlanEvent planEvent = new PlanEvent(USER_ACCEPTED, userId);
        updateEvent(plan, planEvent);
        return PlanEventResultMessage.success(PLAN_UPDATED);
    }

    public PlanEventResultMessage waitList(long planId, long userId){
        if(!planRepository.existsById(planId)) return PlanEventResultMessage.failure(PLAN_NOT_FOUND);
        if(!isDiscordUser(userId)) return PlanEventResultMessage.failure(USER_NOT_FOUND);
        Plan plan = planRepository.getReferenceById(planId);
        PlanUser user = plan.getInvitees().get(userId);
        if(user == null) return PlanEventResultMessage.failure(USER_NOT_IN_PLAN);
        if(plan.getAcceptedIds().size() < plan.getCount()) return PlanEventResultMessage.failure(PLAN_NOT_FULL);
        PlanEvent planEvent = new PlanEvent(USER_WAITLISTED, userId);
        updateEvent(plan, planEvent);
        return PlanEventResultMessage.success(PLAN_UPDATED);
    }

    public PlanEventResultMessage maybe(long planId, long userId){
        if(!planRepository.existsById(planId)) return PlanEventResultMessage.failure(PLAN_NOT_FOUND);
        if(!isDiscordUser(userId)) return PlanEventResultMessage.failure(USER_NOT_FOUND);
        Plan plan = planRepository.getReferenceById(planId);
        PlanUser user = plan.getInvitees().get(userId);
        if(user == null) return PlanEventResultMessage.failure(USER_NOT_IN_PLAN);
        PlanEvent planEvent = new PlanEvent(USER_MAYBED, userId);
        updateEvent(plan, planEvent);
        return PlanEventResultMessage.success(PLAN_UPDATED);
    }

    public PlanEventResultMessage deny(long planId, long userId){
        if(!planRepository.existsById(planId)) return PlanEventResultMessage.failure(PLAN_NOT_FOUND);
        if(!isDiscordUser(userId)) return PlanEventResultMessage.failure(USER_NOT_FOUND);
        Plan plan = planRepository.getReferenceById(planId);
        PlanUser user = plan.getInvitees().get(userId);
        if(user == null) return PlanEventResultMessage.failure(USER_NOT_IN_PLAN);
        PlanEvent planEvent = new PlanEvent(USER_DECLINED, userId);
        updateEvent(plan, planEvent);
        return PlanEventResultMessage.success(PLAN_UPDATED);
    }

    public Plan createPlan(PlanCreationData planData){
        Plan plan = new Plan(planData);
        Set<Long> inviteeIds = new HashSet<>(planData.players());
        Role everyone = discordGuild.getPublicRole();
        planData.roles().stream().filter(roleId -> roleId != everyone.getIdLong()).forEach(roleId -> inviteeIds.addAll(discordGuild.getMembersWithRoles(discordGuild.getRoleById(roleId)).stream().map(ISnowflake::getIdLong).toList()));
        inviteeIds.removeIf(id -> !isValidUser(planData.author(), id, discordNoInviteRoleId, discordGuild));
        if(inviteeIds.isEmpty()) return null;
        inviteeIds.forEach(planUser -> plan.getInvitees().put(planUser, new PlanUser(plan, planUser)));
        Plan savedPlan = planRepository.save(plan); // Save initial plan
        long planChannelMessageId = planTextChannel.sendMessageEmbeds(getPlanChannelMessage(savedPlan, discordGuild)).addActionRow(
                Button.secondary("add_users", "Add users")
        ).complete().getIdLong();
        savedPlan.setMessageId(planChannelMessageId); // Send plan channel message and save id
        long authorMessageId = bot.getUserById(planData.author()).openPrivateChannel().complete().sendMessageEmbeds(getHostMessage(savedPlan, discordGuild)).addActionRow(
                List.of(
                        Button.secondary("send_message", "Send message"),
                        Button.secondary("edit_event", "Edit details"),
                        Button.danger("delete_event", "Delete event")
                )
        ).complete().getIdLong();
        savedPlan.setPrivateMessageId(authorMessageId); // Send author message and save id
        inviteeIds.forEach(memberId -> { // Send invitee messages and save ids
            long pmId = discordGuild.getMemberById(memberId).getUser().openPrivateChannel().complete().sendMessageEmbeds(getPlanPrivateMessage(savedPlan, discordGuild))
                    .addActionRow(getButtons(savedPlan.isFull(), savedPlan.isNeedFillIn(), PlanUser.Status.DECIDING, false))
                    .complete().getIdLong();
            savedPlan.getInvitees().get(memberId).setDiscordNotificationId(pmId);
        });
        return planRepository.save(savedPlan);
    }

    public void addUsersToPlan(Plan plan, Long...userIds) {
        Set<Long> invitees = new HashSet<>(Arrays.asList(userIds));
        invitees.removeIf(id -> !isValidUser(plan.getAuthorId(), id, discordNoInviteRoleId, discordGuild));
        invitees.forEach(memberId -> { // Send invitee messages and save ids
            plan.getInvitees().put(memberId, new PlanUser(plan, memberId));
            long pmId = discordGuild.getMemberById(memberId).getUser().openPrivateChannel().complete().sendMessageEmbeds(getPlanPrivateMessage(plan, discordGuild))
                    .addActionRow(getButtons(plan.isFull(), plan.isNeedFillIn(), PlanUser.Status.DECIDING, false))
                    .complete().getIdLong();
            plan.getInvitees().get(memberId).setDiscordNotificationId(pmId);
        });
        planRepository.save(plan);
        updateMessages(plan);
    }

    public void sendMessage(Plan plan, String message){
        plan.getAcceptedIds().forEach(id -> bot.openPrivateChannelById(id).queue(
                channel -> channel.sendMessage("A message in regards to the plans made for: " + plan.getTitle() + "\n" + message).queue()
        ));
    }

    public void updateEvent(Plan plan, PlanEvent planEvent) {
        if(plan == null) return;

        LinkedList<PlanEvent> processedEvents = plan.processEvents(planEvent);
        // send messages about the processed events
        for(PlanEvent event: processedEvents){
            switch(event.getEvent()){
                case USER_MOVED_FILLIN_TO_ACCEPTED:
                    discordGuild.getMemberById(event.getUid()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage("A member has dropped out of: " + plan.getTitle() + " . You are now moved from filled in to accepted.").queue());
                    break;
                case USER_MOVED_WAITLIST_TO_ACCEPTED:
                    discordGuild.getMemberById(event.getUid()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage("A member has dropped out of " + plan.getTitle() + " . You are now moved from waitlisted to accepted.").queue());
                    break;
                case USER_MOVED_WAITLIST_TO_FILL_IN:
                    discordGuild.getMemberById(event.getUid()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage("A member has requested a fill in for event " + plan.getTitle() + " . You are now moved from waitlisted to a fill-in spot.").queue());
                    break;
                case EVENT_CREATED_FROM_WAITLIST:
                    createNewPlanFromWaitlist(plan);
                    break;
            }
        }

        planRepository.save(plan);
        updateMessages(plan);
    }

    private void createNewPlanFromWaitlist(Plan plan){
        // decline everyone in the old plan
        List<PlanUser> newCrew = plan.getWaitlist()
                .stream()
                .sorted(Comparator.comparing(PlanUser::getWaitlist_time))
                .limit(plan.getCount() + 1).toList();
        List<PlanEvent> declineEvents = newCrew.stream().map(user -> new PlanEvent(USER_DECLINED, user.getId().getUserId())).toList();
        plan.processEvents(declineEvents.toArray(PlanEvent[]::new));
        planRepository.save(plan);
        updateMessages(plan);
        // create a new plan with the new users
        PlanCreationData planCreationData = new PlanCreationData(
                plan.getTitle(),
                plan.getNotes(),
                plan.getDate(),
                newCrew.remove(0).getId().getUserId(),
                newCrew.stream().map(user -> user.getId().getUserId()).toList(),
                List.of(),
                plan.getCount()
        );
        Plan newPlan = new Plan(planCreationData);
        newCrew.stream().map(user -> user.getId().getUserId()).forEach(planUser -> newPlan.getInvitees().put(planUser, new PlanUser(plan, planUser, PlanUser.Status.ACCEPTED)));
        Plan savedPlan = planRepository.save(newPlan); // Save initial plan
        long planChannelMessageId = planTextChannel.sendMessageEmbeds(getPlanChannelMessage(savedPlan, discordGuild)).addActionRow(
                Button.secondary("add_users", "Add users")
        ).complete().getIdLong();
        savedPlan.setMessageId(planChannelMessageId); // Send plan channel message and save id
        long authorMessageId = bot.getUserById(newPlan.getAuthorId()).openPrivateChannel().complete().sendMessageEmbeds(getHostMessage(savedPlan, discordGuild)).addActionRow(
                List.of(
                        Button.secondary("send_message", "Send message"),
                        Button.secondary("edit_event", "Edit details"),
                        Button.danger("delete_event", "Delete event")
                )
        ).complete().getIdLong();
        savedPlan.setPrivateMessageId(authorMessageId); // Send author message and save id
        newCrew.stream().map(user -> user.getId().getUserId()).forEach(memberId -> { // Send invitee messages and save ids
            long pmId = discordGuild.getMemberById(memberId).getUser().openPrivateChannel().complete().sendMessageEmbeds(getPlanPrivateMessage(savedPlan, discordGuild))
                    .addActionRow(getButtons(savedPlan.isFull(), savedPlan.isNeedFillIn(), PlanUser.Status.DECIDING, false))
                    .complete().getIdLong();
            savedPlan.getInvitees().get(memberId).setDiscordNotificationId(pmId);
        });
        planRepository.save(savedPlan);
    }

    public void deletePlan(Plan plan){
        // let the people know
        plan.getAcceptedIds().forEach(acceptedUser ->
                bot.openPrivateChannelById(acceptedUser).queue(privateChannel -> {
                    privateChannel.sendMessage("Event: " + plan.getTitle() + " has been canceled and deleted.").queue();
                })
        );
        plan.getMaybesIds().forEach(acceptedUser ->
                bot.openPrivateChannelById(acceptedUser).queue(privateChannel -> {
                    privateChannel.sendMessage("Event: " + plan.getTitle() + " has been canceled and deleted.").queue();
                })
        );
        // delete guild message
        planTextChannel.editMessageEmbedsById(plan.getMessageId(), getDeletedPlanMessage(plan)).queue();
        planTextChannel.retrieveMessageById(plan.getMessageId()).queue(message -> message.editMessageComponents().queue());
        // delete coordinator message
        bot.openPrivateChannelById(plan.getAuthorId()).queue(
                channel -> channel.retrieveMessageById(plan.getPrivateMessageId()).queue(message -> {
                    message.editMessageEmbeds(getDeletedPlanMessage(plan)).queue();
                    message.editMessageComponents().queue();
                })
        );
        // delete private messages
        plan.getInvitees().forEach((id, user) -> bot.openPrivateChannelById(id).queue(
                channel -> channel.retrieveMessageById(user.getDiscordNotificationId()).queue(message -> {
                    message.editMessageEmbeds(getDeletedPlanMessage(plan)).queue();
                    message.editMessageComponents().queue();
                })
        ));
        // delete from database
        planRepository.deleteById(plan.getId());
    }

    public void updateMessages(Plan plan){
        // update guild message
        try {
            planTextChannel.retrieveMessageById(plan.getMessageId()).queue(
                    message -> message.editMessageEmbeds(getPlanChannelMessage(plan, discordGuild)
            ).queue());
        } catch (Exception e){
            log.error("Error editing public guild message for event {}", plan.getId(), e);
        }
        // update private messages
        for(Long currentUserId: plan.getInvitees().keySet()){
            PlanUser user = plan.getInvitees().get(currentUserId);
            try {
                discordGuild.getMemberById(currentUserId).getUser().openPrivateChannel().queue(channel -> channel.retrieveMessageById(user.getDiscordNotificationId()).queue(message -> {
                    message.editMessageEmbeds(PlanHelper.getPlanPrivateMessage(plan, discordGuild)).queue();

                    boolean full = plan.isFull();
                    boolean needsFillIn = plan.isNeedFillIn();
                    PlanUser.Status userStatus = user.getUserStatus();

                    LinkedList<Button> buttons = PlanHelper.getButtons(full, needsFillIn, userStatus, user.isNeedFillIn());
                    if(buttons.isEmpty()){
                        message.editMessageComponents().queue();
                    } else {
                        message.editMessageComponents(ActionRow.of(buttons)).queue();
                    }
                }));
            } catch (Exception e){
                log.error("Error editing message to private member for event{}", plan.getId(), e);
            }
        }
        // update author message
        try {
            discordGuild.getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().queue(channel -> channel.retrieveMessageById(plan.getPrivateMessageId()).queue(message -> message.editMessageEmbeds(PlanHelper.getHostMessage(plan, discordGuild)).queue()));
        } catch (Exception e){
            log.error("Error editing private message for event {}", plan.getId(), e);
        }
    }

    private boolean isDiscordUser(long uid){
        return bot.getUserById(uid) != null;
    }
}
