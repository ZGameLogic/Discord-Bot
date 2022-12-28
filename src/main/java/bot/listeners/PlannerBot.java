package bot.listeners;

import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import data.database.planData.Plan;
import data.database.planData.PlanRepository;
import data.database.planData.User;
import data.database.userData.UserDataRepository;
import interfaces.TwilioInterface;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class PlannerBot extends AdvancedListenerAdapter {

    private final GuildDataRepository guildData;
    private final PlanRepository planRepository;
    private final UserDataRepository userData;

    public PlannerBot(PlanRepository planRepository, UserDataRepository userData, GuildDataRepository guildData) {
        this.planRepository = planRepository;
        this.userData = userData;
        this.guildData = guildData;
    }

    @ButtonResponse("enable_plan")
    private void enablePlan(ButtonInteractionEvent event){
        event.editButton(Button.success("disable_plan", "Plan bot")).queue();
        Guild guild = event.getGuild();
        long planEventId = guild.upsertCommand(Commands.slash("plan_event", "Plan an event with friends")).complete().getIdLong();
        long textCommandId = guild.upsertCommand(
                Commands.slash("text_notifications", "Enable or disable text message notifications")
                        .addSubcommands(
                                new SubcommandData("enable", "Enables text messaging")
                                        .addOption(OptionType.STRING, "number", "your phone number. EX: 16301112222", true),
                                new SubcommandData("disable", "Disables text messaging")
                        )
        ).complete().getIdLong();
        long eventsChannel = guild.createTextChannel("plans").setTopic("This is a channel for planned events").complete().getIdLong();
        GuildData dbGuild = guildData.getOne(guild.getIdLong());
        dbGuild.setPlanEnabled(true);
        dbGuild.setPlanChannelId(eventsChannel);
        dbGuild.setCreatePlanCommandId(planEventId);
        dbGuild.setTextCommandId(textCommandId);
        guildData.save(dbGuild);
    }

    @ButtonResponse("disable_plan")
    private void disablePlan(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_plan", "Plan bot")).queue();
        Guild guild = event.getGuild();
        GuildData dbGuild = guildData.getOne(guild.getIdLong());
        guild.deleteCommandById(dbGuild.getCreatePlanCommandId()).queue();
        guild.getTextChannelById(dbGuild.getPlanChannelId()).delete().queue();
        dbGuild.setPlanEnabled(false);
        dbGuild.setPlanChannelId(0L);
        dbGuild.setCreatePlanCommandId(0L);
        dbGuild.setTextCommandId(0L);
        guildData.save(dbGuild);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().getIdLong() == 232675572772372481l){
            long eventId = Long.parseLong(event.getMessage().getContentRaw().replace("!", ""));
            Plan plan = planRepository.getOne(eventId);
            Guild guild = event.getJDA().getGuildById(plan.getGuildId());
            plan.getInvitees().forEach((id, user) -> event.getJDA().openPrivateChannelById(id).queue(
                    channel -> channel.retrieveMessageById(user.getMessageId()).queue(
                            message -> message.delete().queue()
                    )
            ));
            // delete from database
            planRepository.deleteById(plan.getId());
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        for(Guild guild: event.getJDA().getGuilds()){
            guild.upsertCommand(
                    Commands.slash("plan_event", "Plan an event with friends")
            ).queue();
            guild.upsertCommand(
                    Commands.slash("text_notifications", "Enable or disable text message notifications")
                            .addSubcommands(
                                    new SubcommandData("enable", "Enables text messaging")
                                            .addOption(OptionType.STRING, "number", "your phone number. EX: 16301112222", true),
                                    new SubcommandData("disable", "Disables text messaging")
                            )
            ).queue();
        }
    }

    @SlashResponse(value = "text_notifications", subCommandName = "enable")
    private void enableTextSlash(SlashCommandInteractionEvent event){
        String formatted = event.getOption("number").getAsString()
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace(" ", "")
                .replace("+", "");
        if(formatted.length() < 10) {
            event.reply("Phone number is too short").queue();
            return;
        }
        try {
            Long number = Long.parseLong(formatted);
            data.database.userData.User user = new data.database.userData.User(
                    event.getUser().getIdLong(),
                    number,
                    event.getUser().getName()
            );
            userData.save(user);
        } catch (NumberFormatException e){
            event.reply("Invalid phone number").queue();
        }
        event.reply("Text notifications for plans enabled").setEphemeral(true).queue();
    }

    @SlashResponse(value = "text_notifications", subCommandName = "disable")
    private void disableTextSlash(SlashCommandInteractionEvent event){
        if(userData.existsById(event.getUser().getIdLong())){
            userData.deleteById(event.getUser().getIdLong());
        }
        event.reply("Text messaging disabled").setEphemeral(true).queue();
    }

    @SlashResponse("plan_event")
    private void planEventSlashCommand(SlashCommandInteractionEvent event){
        TextInput notes = TextInput.create("notes", "Notes about the event", TextInputStyle.SHORT)
                .setPlaceholder("Grinding the event").setRequired(false).build();
        TextInput date = TextInput.create("date", "Date", TextInputStyle.SHORT)
                .setPlaceholder("4/5 9:23am or 7:00pm").build();
        TextInput name = TextInput.create("title", "Title of the event", TextInputStyle.SHORT)
                .setPlaceholder("Hunt Showdown").build();
        TextInput count = TextInput.create("count", "Number of people (not including yourself)", TextInputStyle.SHORT)
                .setPlaceholder("2").build();
        event.replyModal(Modal.create("plan_event_modal", "Details of meeting")
                .addActionRow(name)
                .addActionRow(date)
                .addActionRow(notes)
                .addActionRow(count)
                .build())
                .queue();
    }

    @ModalResponse("edit_event_modal")
    private void editEventModal(ModalInteractionEvent event){
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        String notes = event.getValue("notes").getAsString();
        String dateString = event.getValue("date").getAsString();
        String title = event.getValue("title").getAsString();
        Date date = stringToDate(dateString);
        if(date == null){
            event.reply("Invalid date").setEphemeral(true).queue();
            return;
        }
        int count;
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
        plan.setCount(count);
        plan.setTitle(title);
        plan.setNotes(notes);
        plan.setDate(date);
        plan.addToLog("Event details edited");
        updateMessages(plan, event.getJDA().getGuildById(plan.getGuildId()));
        planRepository.save(plan);
        event.reply("Plan details have been edited").setEphemeral(true).queue();
    }

    @ModalResponse("plan_event_modal")
    private void planEventModalResponse(ModalInteractionEvent event){
        String notes = event.getValue("notes").getAsString();
        String dateString = event.getValue("date").getAsString();
        String title = event.getValue("title").getAsString();
        Date date = stringToDate(dateString);
        if(date == null){
            event.reply("Invalid date").setEphemeral(true).queue();
            return;
        }
        int count;
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
        event.reply("Select people to invite (Don't include yourself). This cannot be changed. Plan id:" + event.getIdLong()).setActionRow(
                        EntitySelectMenu.create("People", EntitySelectMenu.SelectTarget.USER, EntitySelectMenu.SelectTarget.ROLE)
                                .setMinValues(1)
                                .setMaxValues(25)
                                .build())
                .setEphemeral(true)
                .queue(message -> {
                    Plan plan = new Plan();
                    plan.setTitle(title);
                    plan.setChannelId(guildData.getOne(event.getGuild().getIdLong()).getPlanChannelId());
                    plan.setGuildId(event.getGuild().getIdLong());
                    plan.setNotes(notes);
                    plan.setDate(date);
                    plan.setAuthorId(event.getUser().getIdLong());
                    plan.setCount(finalCount);
                    plan.setId(event.getIdLong());
                    plan.addToLog("Created event");
                    planRepository.save(plan);
                });
    }

    @EntitySelectionResponse("People")
    private void planPeople(EntitySelectInteraction event){
        event.deferReply().setEphemeral(true).queue();
        long planId = Long.parseLong(event.getMessage().getContentRaw().split(":")[1]);
        Plan plan = planRepository.getOne(planId);
        GuildData gd = guildData.getOne(event.getGuild().getIdLong());
        if(!plan.getInvitees().isEmpty()){
            event.getHook().sendMessage("You already set the people for this event. You can dismiss this message").setEphemeral(true).queue();
            return;
        }
        HashMap<Long, User> invitees = new HashMap<>();
        for(Member m : event.getMentions().getMembers()){
            if(m.getUser().isBot()){
                event.getHook().sendMessage("You cannot add bots to an event you are planning").setEphemeral(true).queue();
                return;
            }
            if(m.getIdLong() == event.getUser().getIdLong()){
                event.getHook().sendMessage("You cannot add yourself to an event you are planning").setEphemeral(true).queue();
                return;
            }
            invitees.put(m.getIdLong(), new User(m.getIdLong(), 0));
        }
        for(Role r: event.getMentions().getRoles()){
            if(r.getIdLong() == event.getGuild().getPublicRole().getIdLong()) continue;
            for(Member m: event.getGuild().getMembersWithRoles(r)){
                if(m.getUser().isBot()) continue;
                if(m.getIdLong() == event.getUser().getIdLong()) continue;
                if(invitees.containsKey(m.getIdLong())) continue;
                invitees.put(m.getIdLong(), new User(m.getIdLong(), 0));
            }
        }
        plan.setInvitees(invitees);
        for(Long id: invitees.keySet()){
            Member m = event.getGuild().getMemberById(id);
            try {
                PrivateChannel pm = m.getUser().openPrivateChannel().complete();
                Message message = pm.sendMessageEmbeds(EmbedMessageGenerator.singleInvite(plan, event.getGuild()))
                        .addActionRow(Button.success("accept_event", "Accept"),
                                Button.danger("deny_event", "Deny"),
                                Button.primary("maybe_event", "Maybe"))
                        .complete();
                plan.updateMessageIdForUser(m.getIdLong(), message.getIdLong());
                if(userData.existsById(m.getIdLong())){
                    TwilioInterface.sendMessage(
                            userData.getOne(m.getIdLong()).getPhone_number() + "",
                            event.getUser().getName() + " has invited you to " + plan.getTitle() + "." +
                                    " Reply to the invite on discord."
                    );
                }
            } catch (Exception e){
                log.error("Error sending message to member to create event", e);
            }
        }
        try {
            Message message = event.getGuild().getTextChannelById(gd.getPlanChannelId()).sendMessageEmbeds(EmbedMessageGenerator.guildPublicMessage(plan, event.getGuild()))
                    .addActionRow(Button.secondary("add_users", "Add users")).complete();
            plan.setMessageId(message.getIdLong());
            plan.addToLog("Added people to event");
            PrivateChannel channel = event.getGuild().getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().complete();
            Message m = channel.sendMessageEmbeds(EmbedMessageGenerator.creatorMessage(plan, event.getGuild())).complete();
            m.editMessageComponents(
                    ActionRow.of(
                            Button.secondary("send_message", "Send message"),
                            Button.secondary("edit_event", "Edit details"),
                            Button.danger("delete_event", "Delete event")
                    )
            ).queue();
            plan.setPrivateMessageId(m.getIdLong());
            planRepository.save(plan);
        } catch (Exception e){
            log.error("Error sending creating event message reply", e);
        }
        event.getHook().setEphemeral(true).sendMessage("Event created in <#" + gd.getPlanChannelId() + ">").queue();
    }

    @ButtonResponse("add_users")
    private void addUsersButton(ButtonInteractionEvent event){
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        GuildData gd = guildData.getOne(event.getGuild().getIdLong());
        if(plan.getAuthorId() != event.getUser().getIdLong()){
            event.reply("You are not the owner of this event").setEphemeral(true).queue();
            return;
        }
        event.reply("Select people to add to the event. This cannot be changed. Plan id:" + plan.getId()).setActionRow(
                        EntitySelectMenu.create("add_people", EntitySelectMenu.SelectTarget.USER)
                                .setMinValues(1)
                                .setMaxValues(25)
                                .build())
                .setEphemeral(true).queue();
    }

    @EntitySelectionResponse("add_people")
    private void addPeopleResponse(EntitySelectInteractionEvent event){
        event.deferReply().setEphemeral(true).queue();
        long planId = Long.parseLong(event.getMessage().getContentRaw().split(":")[1]);
        Plan plan = planRepository.getOne(planId);
        LinkedList<Member> invitees = new LinkedList<>();
        for(Member m : event.getMentions().getMembers()){
            if(m.getUser().isBot()){
                event.getHook().sendMessage("You cannot add bots to an event you are planning").setEphemeral(true).queue();
                return;
            }
            if(m.getIdLong() == event.getUser().getIdLong()){
                event.getHook().sendMessage("You cannot add yourself to an event you are planning").setEphemeral(true).queue();
                return;
            }
            if(plan.getInvitees().containsKey(m.getIdLong())) continue;
            invitees.add(m);
        }
        for(Member m: invitees){
            Message message = m.getUser().openPrivateChannel().complete().sendMessageEmbeds(EmbedMessageGenerator.singleInvite(plan, event.getGuild()))
                    .addActionRow(Button.success("accept_event", "Accept"),
                            Button.danger("deny_event", "Deny"),
                            Button.primary("maybe_event", "Maybe"))
                    .complete();
            if(userData.existsById(m.getIdLong())){
                TwilioInterface.sendMessage(
                        userData.getOne(m.getIdLong()).getPhone_number() + "",
                        event.getUser().getName() + " has invited you to " + plan.getTitle() + "." +
                                " Reply to the invite on discord."
                );
            }
            plan.addUser(m);
            plan.updateMessageIdForUser(m.getIdLong(), message.getIdLong());
        }
        planRepository.save(plan);
        updateMessages(plan, event.getGuild());
    }

    @ModalResponse("send_message_modal")
    private void sendMessageModal(ModalInteractionEvent event){
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        String message = event.getValue("message").getAsString();
        plan.getAccepted().forEach(id -> event.getJDA().openPrivateChannelById(id).queue(
                channel -> channel.sendMessage("A message in regards to the plans made for: " + plan.getTitle() + "\n" + message).queue()
        ));
        event.reply("Message sent to all accepted people").setEphemeral(true).queue();
    }

    @ButtonResponse("send_message")
    private void sendMessageEvent(ButtonInteraction event){
        TextInput message = TextInput.create("message", "Message to be sent to accepted users", TextInputStyle.PARAGRAPH).build();
        event.replyModal(Modal.create("send_message_modal", "Send message").addActionRow(message) .build()) .queue();
    }

    @ButtonResponse("edit_event")
    private void editDetailsButtonEvent(ButtonInteraction event){
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        TextInput.Builder notesBuilder = TextInput.create("notes", "Notes about the event", TextInputStyle.SHORT).setRequired(false);
        if(plan.getNotes() != null && !plan.getNotes().isEmpty()) notesBuilder.setValue(plan.getNotes());
        TextInput notes = notesBuilder.build();
        TextInput name = TextInput.create("title", "Title of the event", TextInputStyle.SHORT)
                .setValue(plan.getTitle()).build();
        TextInput count = TextInput.create("count", "Number of people looking for", TextInputStyle.SHORT)
                .setValue(plan.getCount() + "").build();
        SimpleDateFormat formatter = new SimpleDateFormat("M/dd h:mma", Locale.ENGLISH);
        String dateString;
        if(plan.getDate() != null) {
            dateString = formatter.format(plan.getDate());
        } else {
            dateString = formatter.format(new Date());
        }
        TextInput date = TextInput.create("date", "Date", TextInputStyle.SHORT)
                .setValue(dateString).build();
        event.replyModal(Modal.create("edit_event_modal", "Details of meeting")
                        .addActionRow(name)
                        .addActionRow(date)
                        .addActionRow(notes)
                        .addActionRow(count)
                        .build())
                .queue();
    }

    @ButtonResponse("delete_event")
    private void deleteEvent(ButtonInteraction event){
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        Guild guild = event.getJDA().getGuildById(plan.getGuildId());
        // delete guild message
        guild.getTextChannelById(plan.getChannelId()).retrieveMessageById(plan.getMessageId()).queue(message -> message.delete().queue());
        // delete coordinator message
        event.getJDA().openPrivateChannelById(plan.getAuthorId()).queue(
                channel -> channel.retrieveMessageById(plan.getPrivateMessageId()).queue(
                        message -> message.delete().queue()
                )
        );
        // delete private messages
        plan.getInvitees().forEach((id, user) -> event.getJDA().openPrivateChannelById(id).queue(
                channel -> channel.retrieveMessageById(user.getMessageId()).queue(
                        message -> message.delete().queue()
                )
        ));
        // delete from database
        planRepository.deleteById(plan.getId());
    }

    @ButtonResponse("drop_out_event")
    private void dropOutEvent(ButtonInteraction event){
        event.editButton(Button.danger("confirm_drop_out_event", "Confirm Dropout")).queue();
    }

    @ButtonResponse("confirm_drop_out_event")
    private void confirmDropOutEvent(ButtonInteraction event){
        long userId = event.getUser().getIdLong();
        event.getMessage().editMessageComponents().queue();
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        Guild guild = event.getJDA().getGuildById(plan.getGuildId());
        plan.planDropOut(userId);
        plan.addToLog(event.getJDA().getUserById(userId).getName() + " dropped out");
        guild.getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage(event.getUser().getName() + " has dropped out of " + plan.getTitle()).queue());
        event.deferEdit().queue();
        // Check to see if anyone is on the waitlist
        if(plan.getWaitlist().size() > 0){
            LinkedList<Long> waitlist = plan.getWaitlist();
            long waitlistId = waitlist.removeFirst();
            plan.planAccepted(waitlistId);
            plan.addToLog(event.getJDA().getUserById(waitlistId).getName() + " has been moved from waitlist");
            guild.getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage(event.getJDA().getUserById(waitlistId).getName() + " accepted " + plan.getTitle()).queue());
            guild.getMemberById(waitlistId).getUser().openPrivateChannel().queue(channel -> channel.sendMessage("You got moved off the waitlist and accepted " + plan.getTitle()).queue());
        }
        planRepository.save(plan);
        updateMessages(plan, guild);
    }

    @ButtonResponse("accept_event")
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

    @ButtonResponse("waitlist_event")
    private void waitlistEvent(ButtonInteraction event){
        long userId = event.getUser().getIdLong();
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        plan.planWaitlist(userId);
        plan.addToLog(event.getJDA().getUserById(userId).getName() + " was added to waitlist");
        event.reply("Added to the waitlist. You will be notified if someone drops out").setEphemeral(true).queue();
        Guild guild = event.getJDA().getGuildById(plan.getGuildId());
        updateMessages(plan, guild);
        planRepository.save(plan);
    }

    @ButtonResponse("deny_event")
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

    @ButtonResponse("maybe_event")
    private void maybeEvent(ButtonInteractionEvent event){
        long userId = event.getUser().getIdLong();
        Plan plan = planRepository.getOne(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        plan.planMaybed(userId);
        plan.addToLog(event.getJDA().getUserById(userId).getName() + " maybed");
        planRepository.save(plan);
        Guild guild = event.getJDA().getGuildById(plan.getGuildId());
        event.deferEdit().queue();
        updateMessages(plan, guild);
    }

    private void updateMessages(Plan plan, Guild guild){
        // Get the state of the plan
        boolean full = plan.isFull();
        // update guild message
        try {
            guild.getTextChannelById(plan.getChannelId()).retrieveMessageById(plan.getMessageId()).queue(message -> message.editMessageEmbeds(EmbedMessageGenerator.guildPublicMessage(plan, guild)).queue());
        } catch (Exception e){
            log.error("Error editing public guild message for event " + plan.getTitle(), e);
        }
        // update private messages
        for(Long currentUserId: plan.getInvitees().keySet()){
            User user = plan.getInvitees().get(currentUserId);
            int state = user.getStatus();
            try {
                guild.getMemberById(currentUserId).getUser().openPrivateChannel().queue(channel -> channel.retrieveMessageById(user.getMessageId()).queue(message -> {
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
                            message.editMessageComponents(
                                    ActionRow.of(
                                            Button.secondary("waitlist_event", "Waitlist"),
                                            Button.danger("deny_event", "Deny")
                                    )
                            ).queue();
                        } else { // if not full and unsure still
                            message.editMessageComponents(
                                    ActionRow.of(
                                            Button.success("accept_event", "Accept"),
                                            Button.danger("deny_event", "Deny"),
                                            Button.primary("maybe_event", "Maybe")
                                    )
                            ).queue();
                        }
                    } else if(state == 3){
                        if(full){
                            message.editMessageComponents(
                                    ActionRow.of(
                                            Button.secondary("waitlist_event", "Waitlist"),
                                            Button.danger("deny_event", "Deny")
                                    )
                            ).queue();
                        } else { // if not full and unsure still
                            message.editMessageComponents(
                                    ActionRow.of(
                                            Button.success("accept_event", "Accept"),
                                            Button.danger("deny_event", "Deny")
                                    )
                            ).queue();
                        }
                    } else if(state == 2){
                        if(full){
                            message.editMessageComponents().queue();
                        }
                    }
                }));
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

    private Date stringToDate(String dateString){
        dateString = dateString.toUpperCase();
        HashMap<String, Integer[]> patterns = new HashMap<>();
        patterns.put("M/dd h:mma", new Integer[]{Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("h:mma", new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        for(String pattern: patterns.keySet()){
            SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.ENGLISH);
            Calendar date = Calendar.getInstance();
            Calendar formatted = Calendar.getInstance();
            try {
                formatted.setTime(formatter.parse(dateString));
                for(int field: patterns.get(pattern)){
                    date.set(field, formatted.get(field));
                }
                return date.getTime();
            } catch(ParseException ignored){

            }
        }
        return null;
    }
}
