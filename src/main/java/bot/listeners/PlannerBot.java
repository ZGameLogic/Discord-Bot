package bot.listeners;

import bot.utils.Helpers;
import bot.utils.PlanHelper;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import data.database.planData.Plan;
import data.database.planData.PlanRepository;
import data.database.planData.User;
import data.database.userData.UserDataRepository;
import data.intermediates.planData.PlanEvent;
import org.springframework.web.bind.annotation.RestController;
import services.TwilioService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static bot.utils.Helpers.stringToDate;
import static bot.utils.PlanHelper.getPlanChannelMessage;
import static data.database.planData.User.Status.*;
import static data.intermediates.planData.PlanEvent.Event.*;

@Slf4j
@DiscordController
@RestController
public class PlannerBot {

    private final GuildDataRepository guildData;
    private final PlanRepository planRepository;
    private final UserDataRepository userData;
    private final TwilioService twilioService;
    private JDA bot;

    public PlannerBot(PlanRepository planRepository, UserDataRepository userData, GuildDataRepository guildData, TwilioService twilioService) {
        this.planRepository = planRepository;
        this.userData = userData;
        this.guildData = guildData;
        this.twilioService = twilioService;
    }

    @DiscordMapping(Id = "enable_plan")
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
        GuildData dbGuild = guildData.getReferenceById(guild.getIdLong());
        dbGuild.setPlanEnabled(true);
        dbGuild.setPlanChannelId(eventsChannel);
        dbGuild.setCreatePlanCommandId(planEventId);
        dbGuild.setTextCommandId(textCommandId);
        guildData.save(dbGuild);
    }

    @DiscordMapping(Id = "disable_plan")
    private void disablePlan(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_plan", "Plan bot")).queue();
        Guild guild = event.getGuild();
        GuildData dbGuild = guildData.getReferenceById(guild.getIdLong());
        guild.deleteCommandById(dbGuild.getCreatePlanCommandId()).queue();
        guild.getTextChannelById(dbGuild.getPlanChannelId()).delete().queue();
        dbGuild.setPlanEnabled(false);
        dbGuild.setPlanChannelId(0L);
        dbGuild.setCreatePlanCommandId(0L);
        dbGuild.setTextCommandId(0L);
        guildData.save(dbGuild);
    }

    @DiscordMapping
    public void ready(ReadyEvent event) {
        bot = event.getJDA();
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

    @DiscordMapping(Id = "text_notifications", SubId = "enable")
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

    @DiscordMapping(Id = "text_notifications", SubId = "disable")
    private void disableTextSlash(SlashCommandInteractionEvent event){
        if(userData.existsById(event.getUser().getIdLong())){
            userData.deleteById(event.getUser().getIdLong());
        }
        event.reply("Text messaging disabled").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "plan_event")
    private void planEventSlashCommand(SlashCommandInteractionEvent event){
        TextInput notes = TextInput.create("notes", "Notes about the event", TextInputStyle.SHORT)
                .setPlaceholder("Grinding the event").setRequired(false).build();
        TextInput date = TextInput.create("date", "Date and time", TextInputStyle.SHORT)
                .setPlaceholder("Central time zone. Examples: 4/5 9:23am, 7:00pm, tomorrow 6:00pm").build();
        TextInput name = TextInput.create("title", "Title of the event", TextInputStyle.SHORT)
                .setPlaceholder("Hunt Showdown").build();
        TextInput count = TextInput.create("count", "Number of people (not including yourself)", TextInputStyle.SHORT)
                .setPlaceholder("Leave empty for infinite").setRequired(false).build();
        event.replyModal(Modal.create("plan_event_modal", "Details of meeting")
                .addActionRow(name)
                .addActionRow(date)
                .addActionRow(notes)
                .addActionRow(count)
                .build())
                .queue();
    }

    @DiscordMapping(Id = "edit_event_modal")
    private void editEventModal(ModalInteractionEvent event){
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        String notes = event.getValue("notes").getAsString();
        String dateString = event.getValue("date").getAsString();
        String title = event.getValue("title").getAsString();
        Date date = stringToDate(dateString);
        if(date == null){
            event.reply(Helpers.STD_HELPER_MESSAGE).setEphemeral(true).queue();
            return;
        }
        int count;
        try {
            if(!event.getValue("count").getAsString().isEmpty()) {
                count = Integer.parseInt(event.getValue("count").getAsString());
            } else count = -1;
        } catch (NumberFormatException e){
            event.reply("Invalid number").setEphemeral(true).queue();
            return;
        }
        if(count < 1 && count != -1){
            event.reply("Invalid number").setEphemeral(true).queue();
            return;
        }
        plan.setCount(count);
        plan.setTitle(title);
        plan.setNotes(notes);
        plan.setDate(date);
        updateMessages(plan, event.getJDA().getGuildById(plan.getGuildId()));
        planRepository.save(plan);
        event.reply("Plan details have been edited").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "plan_event_modal")
    private void planEventModalResponse(ModalInteractionEvent event){
        String notes = event.getValue("notes").getAsString();
        String dateString = event.getValue("date").getAsString();
        String title = event.getValue("title").getAsString();
        Date date = stringToDate(dateString);
        if(date == null){
            event.reply("""
                    Invalid date and time. Here are some examples of valid dates:
                    7:00pm
                    Today at 7:00pm
                    Tomorrow 6:00pm
                    3/20/2022 4:15pm
                    6/20 3:45pm
                    Wednesday at 4:00pm""").setEphemeral(true).queue();
            return;
        }
        int count;
        try {
            if(!event.getValue("count").getAsString().isEmpty()) {
                count = Integer.parseInt(event.getValue("count").getAsString());
            } else count = -1;
        } catch (NumberFormatException e){
            event.reply("Invalid number").setEphemeral(true).queue();
            return;
        }
        if(count < 1 && count != -1){
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
                    plan.setChannelId(guildData.getReferenceById(event.getGuild().getIdLong()).getPlanChannelId());
                    plan.setGuildId(event.getGuild().getIdLong());
                    plan.setNotes(notes);
                    plan.setDate(date);
                    plan.setAuthorId(event.getUser().getIdLong());
                    plan.setCount(finalCount);
                    plan.setId(event.getIdLong());
                    planRepository.save(plan);
                });
    }

    @DiscordMapping(Id = "People")
    private void planPeople(EntitySelectInteractionEvent event){
        event.deferReply().setEphemeral(true).queue();
        long planId = Long.parseLong(event.getMessage().getContentRaw().split(":")[1]);
        Plan plan = planRepository.getReferenceById(planId);
        GuildData gd = guildData.getReferenceById(event.getGuild().getIdLong());
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
            if(m.getRoles().contains(event.getGuild().getRoleById(1115409055184322680L))) continue; //skip anyone with the no plan
            invitees.put(m.getIdLong(), new User(m.getIdLong(), DECIDING));
        }
        for(Role r: event.getMentions().getRoles()){
            if(r.getIdLong() == event.getGuild().getPublicRole().getIdLong()) continue;
            for(Member m: event.getGuild().getMembersWithRoles(r)){
                if(m.getUser().isBot()) continue;
                if(m.getIdLong() == event.getUser().getIdLong()) continue;
                if(invitees.containsKey(m.getIdLong())) continue;
                if(m.getRoles().contains(event.getGuild().getRoleById(1115409055184322680L))) continue; //skip anyone with the no plan
                invitees.put(m.getIdLong(), new User(m.getIdLong(), DECIDING));
            }
        }
        plan.setInvitees(invitees);
        for(Long id: invitees.keySet()){
            Member m = event.getGuild().getMemberById(id);
            try {
                PrivateChannel pm = m.getUser().openPrivateChannel().complete();
                Message message = pm.sendMessageEmbeds(PlanHelper.getPlanPrivateMessage(plan, event.getGuild()))
                        .addActionRow(Button.success("accept_event", "Accept"),
                                Button.danger("deny_event", "Deny"),
                                Button.primary("maybe_event", "Maybe"))
                        .complete();
                plan.updateMessageIdForUser(m.getIdLong(), message.getIdLong());
                if(userData.existsById(m.getIdLong())){
                    twilioService.sendMessage(
                            String.valueOf(userData.getReferenceById(m.getIdLong()).getPhone_number()),
                            event.getUser().getName() + " has invited you to " + plan.getTitle() + "." +
                                    " Reply to the invite on discord."
                    );
                }
            } catch (Exception e){
                log.error("Error sending message to member to create event", e);
            }
        }
        try {
            Message message = event.getGuild().getTextChannelById(gd.getPlanChannelId()).sendMessageEmbeds(getPlanChannelMessage(plan, event.getGuild()))
                    .addActionRow(Button.secondary("add_users", "Add users")).complete();
            plan.setMessageId(message.getIdLong());
            PrivateChannel channel = event.getGuild().getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().complete();
            Message m = channel.sendMessageEmbeds(PlanHelper.getHostMessage(plan, event.getGuild())).complete();
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

    @DiscordMapping(Id = "add_users")
    private void addUsersButton(ButtonInteractionEvent event){
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
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

    @DiscordMapping(Id = "add_people")
    private void addPeopleResponse(EntitySelectInteractionEvent event){
        event.deferReply().setEphemeral(true).queue();
        long planId = Long.parseLong(event.getMessage().getContentRaw().split(":")[1]);
        Plan plan = planRepository.getReferenceById(planId);
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
            if(m.getRoles().contains(event.getGuild().getRoleById(1115409055184322680L))) continue; //skip anyone with the no plan
            invitees.add(m);
        }
        for(Member m: invitees){
            Message message = m.getUser().openPrivateChannel().complete().sendMessageEmbeds(PlanHelper.getPlanPrivateMessage(plan, event.getGuild()))
                    .addActionRow(Button.success("accept_event", "Accept"),
                            Button.danger("deny_event", "Deny"),
                            Button.primary("maybe_event", "Maybe"))
                    .complete();
            if(userData.existsById(m.getIdLong())){
                twilioService.sendMessage(
                        String.valueOf(userData.getReferenceById(m.getIdLong()).getPhone_number()),
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

    @DiscordMapping(Id = "send_message_modal")
    private void sendMessageModal(ModalInteractionEvent event){
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        String message = event.getValue("message").getAsString();
        plan.getAccepted().forEach(id -> event.getJDA().openPrivateChannelById(id).queue(
                channel -> channel.sendMessage("A message in regards to the plans made for: " + plan.getTitle() + "\n" + message).queue()
        ));
        event.reply("Message sent to all accepted people").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "send_message")
    private void sendMessageEvent(ButtonInteractionEvent event){
        TextInput message = TextInput.create("message", "Message to be sent to accepted users", TextInputStyle.PARAGRAPH).build();
        event.replyModal(Modal.create("send_message_modal", "Send message").addActionRow(message) .build()) .queue();
    }

    @DiscordMapping(Id = "edit_event")
    private void editDetailsButtonEvent(ButtonInteractionEvent event){
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        TextInput.Builder notesBuilder = TextInput.create("notes", "Notes about the event", TextInputStyle.SHORT).setRequired(false);
        if(plan.getNotes() != null && !plan.getNotes().isEmpty()) notesBuilder.setValue(plan.getNotes());
        TextInput notes = notesBuilder.build();
        TextInput name = TextInput.create("title", "Title of the event", TextInputStyle.SHORT)
                .setValue(plan.getTitle()).build();
        TextInput count = TextInput.create("count", "Number of people looking for", TextInputStyle.SHORT)
                .setValue(String.valueOf(plan.getCount())).setRequired(false).build();
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

    @DiscordMapping(Id = "delete_event")
    private void deleteEvent(ButtonInteractionEvent event){
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        Guild guild = event.getJDA().getGuildById(plan.getGuildId());
        // let the people know
        plan.getAccepted().forEach(acceptedUser ->
            guild.getJDA().openPrivateChannelById(acceptedUser).queue(privateChannel -> {
                privateChannel.sendMessage("Event: " + plan.getTitle() + " has been canceled and deleted.").queue();
            })
        );
        plan.getMaybes().forEach(acceptedUser ->
                guild.getJDA().openPrivateChannelById(acceptedUser).queue(privateChannel -> {
                    privateChannel.sendMessage("Event: " + plan.getTitle() + " has been canceled and deleted.").queue();
                })
        );
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

    @DiscordMapping(Id = "request_fill_in")
    private void requestFillIn(ButtonInteractionEvent event){
        long userId = event.getUser().getIdLong();
        PlanEvent planEvent = new PlanEvent(USER_REGISTERED_FOR_FILL_IN, userId);
        updateEvent(planEvent, event);
    }

    @DiscordMapping(Id = "fill_in")
    private void fillIn(ButtonInteractionEvent event){
        long userId = event.getUser().getIdLong();
        PlanEvent planEvent = new PlanEvent(USER_FILLINED, userId);
        updateEvent(planEvent, event);
    }

    @DiscordMapping(Id = "drop_out_event")
    private void dropOutEvent(ButtonInteractionEvent event){
        event.editButton(Button.danger("confirm_drop_out_event", "Confirm Dropout")).queue();
    }

    @DiscordMapping(Id = "confirm_drop_out_event")
    private void confirmDropOutEvent(ButtonInteractionEvent event){
        long userId = event.getUser().getIdLong();
        PlanEvent planEvent = new PlanEvent(USER_DROPPED_OUT, userId);
        updateEvent(planEvent, event);
    }

    @DiscordMapping(Id = "accept_event")
    private void acceptEvent(ButtonInteractionEvent event){
        long userId = event.getUser().getIdLong();
        PlanEvent planEvent = new PlanEvent(USER_ACCEPTED, userId);
        updateEvent(planEvent, event);
    }

    @DiscordMapping(Id = "waitlist_event")
    private void waitlistEvent(ButtonInteractionEvent event){
        long userId = event.getUser().getIdLong();
        PlanEvent planEvent = new PlanEvent(USER_WAITLISTED, userId);
        updateEvent(planEvent, event);
    }

    @DiscordMapping(Id = "deny_event")
    private void denyEvent(ButtonInteractionEvent event){
        long userId = event.getUser().getIdLong();
        PlanEvent planEvent = new PlanEvent(USER_DECLINED, userId);
        updateEvent(planEvent, event);
    }

    @DiscordMapping(Id = "maybe_event")
    private void maybeEvent(ButtonInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        PlanEvent planEvent = new PlanEvent(USER_MAYBED, userId);
        updateEvent(planEvent, event);
    }

    @PostMapping(value = "/sms")
    private void receiveMessage(@RequestBody String body) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI("?" + body), StandardCharsets.UTF_8);
        Map<String, String> mapped = new HashMap<>();
        for (NameValuePair param : params) {
            mapped.put(param.getName(), param.getValue());
        }
        bot.getUserById(232675572772372481L).openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Text message received from number: " + mapped.get("From") + "\n" +
                        "Body: " + mapped.get("Body"))
                .addActionRow(Button.primary("reply_text", "respond")).queue());
        log.info("Text message from: " + mapped.get("From"));
        log.info("Body: " + mapped.get("Body"));
    }

    private void updateEvent(PlanEvent planEvent, ButtonInteractionEvent buttonEvent){
        buttonEvent.deferEdit().queue();
        Plan plan = planRepository.getReferenceById(Long.parseLong(buttonEvent.getMessage().getEmbeds().get(0).getFooter().getText()));
        String title = plan.getTitle();
        Guild guild = buttonEvent.getJDA().getGuildById(plan.getGuildId());
        LinkedList<PlanEvent> processedEvents = plan.processEvents(planEvent);
        // send messages about the processed events
        for(PlanEvent event: processedEvents){
            switch(event.getEvent()){
                case USER_MOVED_FILLIN_TO_ACCEPTED:
                    guild.getMemberById(event.getUid()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage("A member has dropped out of: " + title + " . You are now moved from filled in to accepted.").queue());
                    break;
                case USER_MOVED_WAITLIST_TO_ACCEPTED:
                    guild.getMemberById(event.getUid()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage("A member has dropped out of " + title + " . You are now moved from waitlisted to accepted.").queue());
                    break;
                case USER_MOVED_WAITLIST_TO_FILL_IN:
                    guild.getMemberById(event.getUid()).getUser().openPrivateChannel().queue(channel -> channel.sendMessage("A member has requested a fill in for event " + title + " . You are now moved from waitlisted to a fill-in spot.").queue());
                    break;
                case EVENT_CREATED_FROM_WAITLIST:
                    createNewPlanFromWaitlist(plan, guild, buttonEvent.getIdLong());
                    break;
            }
        }

        updateMessages(plan, guild);
        planRepository.save(plan);
    }

    private void createNewPlanFromWaitlist(Plan plan, Guild guild, long planId) {
        Plan newPlan = plan.createPlanFromWaitlist();
        newPlan.setId(planId);
        for(Long id: newPlan.getInvitees().keySet()){
            Member m = guild.getMemberById(id);
            try {
                PrivateChannel pm = m.getUser().openPrivateChannel().complete();
                Message message = pm.sendMessageEmbeds(PlanHelper.getPlanPrivateMessage(plan, guild))
                        .complete();
                newPlan.updateMessageIdForUser(m.getIdLong(), message.getIdLong());
                if(userData.existsById(m.getIdLong())){
                    twilioService.sendMessage(
                            String.valueOf(userData.getReferenceById(m.getIdLong()).getPhone_number()),
                            "The waitlist for the plan: " + newPlan.getTitle() + " is big enough for a new plan to be created. " +
                                    "Since you were waitlisted on the original plan, you have been automatically added ot the new one."
                    );
                }
            } catch (Exception e){
                log.error("Error sending message to member to create event", e);
            }
        }
        try {
            Message message = guild.getTextChannelById(newPlan.getChannelId()).sendMessageEmbeds(getPlanChannelMessage(newPlan, guild))
                    .addActionRow(Button.secondary("add_users", "Add users")).complete();
            newPlan.setMessageId(message.getIdLong());
            PrivateChannel channel = guild.getMemberById(newPlan.getAuthorId()).getUser().openPrivateChannel().complete();
            Message m = channel.sendMessageEmbeds(PlanHelper.getHostMessage(newPlan, guild)).complete();
            m.editMessageComponents(
                    ActionRow.of(
                            Button.secondary("send_message", "Send message"),
                            Button.secondary("edit_event", "Edit details"),
                            Button.danger("delete_event", "Delete event")
                    )
            ).queue();
            newPlan.setPrivateMessageId(m.getIdLong());
        } catch (Exception ignored) {}
        updateMessages(newPlan, guild);
        planRepository.save(newPlan);
    }

    private void updateMessages(Plan plan, Guild guild){
        // update guild message
        try {
            guild.getTextChannelById(plan.getChannelId()).retrieveMessageById(plan.getMessageId()).queue(
                        message -> message.editMessageEmbeds(getPlanChannelMessage(plan, guild)
                    ).queue());
        } catch (Exception e){
            log.error("Error editing public guild message for event " + plan.getTitle(), e);
        }
        // update private messages
        for(Long currentUserId: plan.getInvitees().keySet()){
            User user = plan.getInvitees().get(currentUserId);
            try {
                guild.getMemberById(currentUserId).getUser().openPrivateChannel().queue(channel -> channel.retrieveMessageById(user.getMessageId()).queue(message -> {
                    message.editMessageEmbeds(PlanHelper.getPlanPrivateMessage(plan, guild)).queue();

                    boolean full = plan.isFull();
                    boolean needsFillIn = plan.isNeedFillIn();
                    User.Status userStatus = user.getUserStatus();

                    LinkedList<Button> buttons = PlanHelper.getButtons(full, needsFillIn, userStatus, user.getNeedFillIn());
                    if(buttons.isEmpty()){
                        message.editMessageComponents().queue();
                    } else {
                        message.editMessageComponents(ActionRow.of(buttons)).queue();
                    }
                }));
            } catch (Exception e){
                log.error("Error editing message to private member", e);
            }
        }
        try {
            guild.getMemberById(plan.getAuthorId()).getUser().openPrivateChannel().queue(channel -> channel.retrieveMessageById(plan.getPrivateMessageId()).queue(message -> message.editMessageEmbeds(PlanHelper.getHostMessage(plan, guild)).queue()));
        } catch (Exception e){
            log.error("Error editing private message for event " + plan.getTitle(), e);
        }
    }
}
