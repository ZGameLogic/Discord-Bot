package bot.listeners;

import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.planData.Plan;
import data.database.planData.PlanRepository;
import data.database.planData.User;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.apache.juli.logging.Log;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Slf4j
public class PlannerBot extends AdvancedListenerAdapter {

    private final PlanRepository planRepository;

    public PlannerBot(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        for(Guild guild: event.getJDA().getGuilds()){
            guild.upsertCommand(
                    Commands.slash("plan_event", "Plan an event with friends")
            ).queue();
        }
    }

    @SlashResponse(commandName = "plan_event")
    private void planEventSlashCommand(SlashCommandInteractionEvent event){
        TextInput time = TextInput.create("notes", "Notes about the event", TextInputStyle.SHORT)
                .setPlaceholder("Today at 4:30pm").build();
        TextInput name = TextInput.create("title", "Title of the event", TextInputStyle.SHORT)
                .setPlaceholder("Hunt Showdown").build();
        TextInput count = TextInput.create("count", "Number of people looking for", TextInputStyle.SHORT)
                .setPlaceholder("2").build();
        event.replyModal(Modal.create("plan_event_modal", "Details of meeting")
                .addActionRow(name)
                .addActionRow(time)
                .addActionRow(count)
                .build())
                .queue();
    }

    @ModalResponse(modalName = "plan_event_modal")
    private void planEventModalResponse(ModalInteractionEvent event){
        String notes = event.getValue("notes").getAsString();
        String title = event.getValue("title").getAsString();
        int count = 0;
        try {
            count = Integer.parseInt(event.getValue("count").getAsString());
        } catch (NumberFormatException e){
            event.reply("Invalid number").setEphemeral(true).queue();
            return;
        }
        if(count < 1){
            event.reply("Invalid number").setEphemeral(true).queue();
            return;
        }
        int finalCount = count;
        event.reply("Select people to invite (Don't include yourself). Plan id:" + event.getIdLong()).setActionRow(
                        EntitySelectMenu.create("People", EntitySelectMenu.SelectTarget.USER)
                                .setMinValues(1)
                                .setMaxValues(10)
                                .build())
                .setEphemeral(true)
                .queue(message -> {
                    Plan plan = new Plan();
                    plan.setTitle(title);
                    plan.setChannelId(event.getChannel().getIdLong());
                    plan.setGuildId(event.getGuild().getIdLong());
                    plan.setNotes(notes);
                    plan.setAuthorId(event.getUser().getIdLong());
                    plan.setCount(finalCount);
                    plan.setId(event.getIdLong());
                    plan.addToLog("Created event");
                    planRepository.save(plan);
                });
    }

    @EntitySelectionResponse(menuId = "People")
    private void planPeople(EntitySelectInteraction event){
        long planId = Long.parseLong(event.getMessage().getContentRaw().split(":")[1]);
        Plan plan = planRepository.getOne(planId);
        if(!plan.getInvitees().isEmpty()){
            event.reply("You already set the people for this event. You can dismiss this message").setEphemeral(true).queue();
            return;
        }
        HashMap<Long, User> invitees = new HashMap<>();
        for(Member m : event.getMentions().getMembers()){
            if(m.getUser().isBot()){
                event.reply("You cannot add bots to an event you are planning").setEphemeral(true).queue();
                return;
            }
            if(m.getIdLong() == event.getUser().getIdLong()){
                event.reply("You cannot add yourself to an event you are planning").setEphemeral(true).queue();
                return;
            }
            invitees.put(m.getIdLong(), new User(m.getIdLong(), 0));
        }
        plan.setInvitees(invitees);
        for(Member m : event.getMentions().getMembers()){
            try {
                PrivateChannel pm = m.getUser().openPrivateChannel().complete();
                Message message = pm.sendMessageEmbeds(EmbedMessageGenerator.singleInvite(plan, event.getGuild()))
                        .addActionRow(Button.success("accept_event", "Accept"),
                                Button.danger("deny_event", "Deny"))
                        .complete();
                plan.updateMessageIdForUser(m.getIdLong(), message.getIdLong());
            } catch (Exception e){
                log.error("Error sending message to member to create event", e);
            }
        }
        try {
            event.reply("plan created").setEphemeral(true).queue();
        } catch (Exception e){

        }
        Message message = event.getChannel().sendMessageEmbeds(EmbedMessageGenerator.plan(plan, event.getGuild())).complete();
        plan.setMessageId(message.getIdLong());
        plan.addToLog("Added people to event");
        PrivateChannel channel = event.getGuild().getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().complete();
        Message m = channel.sendMessageEmbeds(EmbedMessageGenerator.creatorMessage(plan, event.getGuild())).complete();
        m.editMessageComponents(
                ActionRow.of(
                        Button.secondary("send_message", "Send message"),
                        Button.danger("delete_event", "Delete event")
                )
        ).queue();
        plan.setPrivateMessageId(m.getIdLong());
        planRepository.save(plan);
    }

    @ButtonResponse(buttonId = "drop_out_event")
    private void dropOutEvent(ButtonInteraction event){
        long userId = event.getUser().getIdLong();
        event.getMessage().editMessageComponents().queue();
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        Guild guild = event.getJDA().getGuildById(plan.getGuildId());
        plan.planDropOut(userId);
        plan.addToLog(event.getJDA().getUserById(userId).getName() + " dropped out");
        guild.getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage(event.getUser().getName() + " has dropped out of " + plan.getTitle()).queue());
        planRepository.save(plan);
        event.deferEdit().queue();
        updateMessages(plan, guild);
    }

    @ButtonResponse(buttonId = "accept_event")
    private void acceptEvent(ButtonInteraction event){
        long userId = event.getUser().getIdLong();
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        plan.planAccepted(userId);
        plan.addToLog(event.getJDA().getUserById(userId).getName() + " accepted");
        planRepository.save(plan);
        Guild guild = event.getJDA().getGuildById(plan.getGuildId());
        guild.getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage(event.getUser().getName() + " has accepted to join " + plan.getTitle()).queue());
        event.deferEdit().queue();
        updateMessages(plan, guild);
    }

    @ButtonResponse(buttonId = "deny_event")
    private void denyEvent(ButtonInteraction event){
        long userId = event.getUser().getIdLong();
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        plan.planDeclined(userId);
        plan.addToLog(event.getJDA().getUserById(userId).getName() + " declined");
        planRepository.save(plan);
        Guild guild = event.getJDA().getGuildById(plan.getGuildId());
        guild.getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage(event.getUser().getName() + " has declined to join " + plan.getTitle()).queue());
        event.deferEdit().queue();
        updateMessages(plan, guild);
    }

    private void updateMessages(Plan plan, Guild guild){
        // Get the state of the plan
        boolean full = plan.isFull();
        // update guild message
        try {
            guild.getTextChannelById(plan.getChannelId()).retrieveMessageById(plan.getMessageId()).queue(message -> message.editMessageEmbeds(EmbedMessageGenerator.plan(plan, guild)).queue());
        } catch (Exception e){
            log.error("Error editing public guild message for event " + plan.getTitle(), e);
        }
        // update private messages
        for(Long currentUserId: plan.getInvitees().keySet()){
            User user = plan.getInvitees().get(currentUserId);
            int state = user.getStatus();
            try {
                guild.getMemberById(currentUserId).getUser().openPrivateChannel().queue(channel -> {
                    channel.retrieveMessageById(user.getMessageId()).queue(message -> {
                        message.editMessageEmbeds(EmbedMessageGenerator.singleInvite(plan, guild)).queue();
                        if(state == 1){ // if user accepted
                            message.editMessageComponents(
                                    ActionRow.of(
                                            Button.danger("drop_out_event", "Drop out")
                                    )
                            ).queue();
                        } else if(state == -1){ // if user declined
                            message.editMessageComponents().queue();
                        } else if(state == 0){
                            if(full){
                                message.editMessageComponents().queue();
                            } else { // if not full and unsure still
                                message.editMessageComponents(
                                        ActionRow.of(
                                                Button.success("accept_event", "Accept"),
                                                Button.danger("deny_event", "Deny")
                                        )
                                ).queue();
                            }
                        }
                    });
                });
            } catch (Exception e){
                log.error("Error editing message to private member", e);
            }
        }
        try {
            guild.getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().queue(channel -> channel.retrieveMessageById(plan.getPrivateMessageId()).queue(message -> message.editMessageEmbeds(EmbedMessageGenerator.creatorMessage(plan, guild)).queue()));
        } catch (Exception e){
            log.error("Error editing private message for event " + plan.getTitle(), e);
        }
    }
}
