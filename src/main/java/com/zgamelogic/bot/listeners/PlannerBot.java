package com.zgamelogic.bot.listeners;

import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.DiscordMapping;
import com.zgamelogic.discord.annotations.EventProperty;
import com.zgamelogic.bot.utils.EmbedMessageGenerator;
import com.zgamelogic.data.database.planData.linkedMessage.LinkedMessageRepository;
import com.zgamelogic.data.database.planData.plan.Plan;
import com.zgamelogic.data.database.planData.plan.PlanRepository;
import com.zgamelogic.data.database.planData.poll.PollVotes;
import com.zgamelogic.data.database.planData.poll.PollVotesRepository;
import com.zgamelogic.data.database.planData.user.PlanUser;
import com.zgamelogic.data.database.userData.User;
import com.zgamelogic.data.database.userData.UserDataRepository;
import com.zgamelogic.data.intermediates.dataotter.ButtonCommandRock;
import com.zgamelogic.data.intermediates.dataotter.ModalCommandRock;
import com.zgamelogic.data.intermediates.dataotter.SlashCommandRock;
import com.zgamelogic.data.intermediates.planData.DiscordRoleData;
import com.zgamelogic.data.intermediates.planData.DiscordUserData;
import com.zgamelogic.data.intermediates.planData.PlanEvent;
import com.zgamelogic.data.plan.PlanCreationData;
import com.zgamelogic.data.plan.PlanEventResultMessage;
import com.zgamelogic.data.plan.PlanModalData;
import com.zgamelogic.data.plan.PlanModalDataDateless;
import com.zgamelogic.dataotter.DataOtterService;
import com.zgamelogic.services.PlanService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.poll.MessagePollVoteAddEvent;
import net.dv8tion.jda.api.events.message.poll.MessagePollVoteRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.zgamelogic.bot.utils.Helpers.STD_HELPER_MESSAGE;
import static com.zgamelogic.bot.utils.Helpers.stringToDate;
import static com.zgamelogic.bot.utils.PlanHelper.getPlanChannelMessage;
import static com.zgamelogic.bot.utils.PlanHelper.getPrePlanMessage;

@Slf4j
@DiscordController
@RestController
public class PlannerBot {
    @Value("${discord.guild}")
    private long guildId;
    @Value("${discord.plan.id}")
    private long discordPlanId;

    private final PollVotesRepository pollVotesRepository;
    private final PlanRepository planRepository;
    private final UserDataRepository userDataRepository;
    private final PlanService planService;
    private final LinkedMessageRepository linkedMessageRepository;
    private final DataOtterService dataOtterService;

    private JDA bot;

    public PlannerBot(PlanRepository planRepository, UserDataRepository userDataRepository, PlanService planService, LinkedMessageRepository linkedMessageRepository, DataOtterService dataOtterService, PollVotesRepository pollVotesRepository) {
        this.planRepository = planRepository;
        this.userDataRepository = userDataRepository;
        this.planService = planService;
        this.linkedMessageRepository = linkedMessageRepository;
        this.dataOtterService = dataOtterService;
        this.pollVotesRepository = pollVotesRepository;
    }

    @Bean
    private List<CommandData> plannerCommands(){
        return List.of(
                Commands.slash("plan", "Plan things")
                        .addSubcommands(
                            new SubcommandData("help", "Description of how this works"),
                            new SubcommandData("event", "Plan an event with friends"),
                            new SubcommandData("notifications", "Enable or disable notifications")
                                    .addOption(OptionType.BOOLEAN, "receive", "Whether or not you want notifications", true),
                            new SubcommandData("link", "Adds a copy of a plan to a channel")
                                    .addOption(OptionType.STRING, "name", "The name of the plan to link to", false, true)
                                    .addOption(OptionType.STRING, "id", "The id of the plan to link to", false, true)
                        ),
                Commands.slash("text_notifications", "Enable or disable text message notifications")
                        .addSubcommands(
                                new SubcommandData("enable", "Enables text messaging")
                                        .addOption(OptionType.STRING, "number", "your phone number. EX: 16301112222", true),
                                new SubcommandData("disable", "Disables text messaging")),
                Commands.message("link_poll")
        );
    }

    @GetMapping("/plan/users")
    private List<DiscordUserData> getUsers(){
        return bot.getGuildById(guildId).getMembers().stream().map(user ->
                new DiscordUserData(user.getEffectiveName(), user.getUser().getAvatarId(), user.getIdLong())
        ).toList();
    }

    @GetMapping("/plan/roles")
    private List<DiscordRoleData> getRoles(){
        return bot.getGuildById(guildId).getRoles().stream()
                .filter(role -> role.getIdLong() != bot.getGuildById(guildId).getPublicRole().getIdLong())
                .map(role -> new DiscordRoleData(role.getName(), role.getIdLong(), role.getColor()))
                .toList();
    }

    @DiscordMapping
    private void onReady(ReadyEvent event) {
        bot = event.getJDA();
    }

    @DiscordMapping(Id = "link_poll")
    private void linkPlanToPoll(MessageContextInteractionEvent event){
        MessagePoll poll = event.getTarget().getPoll();
        if(poll == null || poll.isExpired()) {
            event.reply("This can only be used on open polls.").setEphemeral(true).queue();
            return;
        }
        for(MessagePoll.Answer a: poll.getAnswers()){
            Date d = stringToDate(a.getText());
            if(d == null){
                event.reply("One or more of answers to this poll are not valid dates.").setEphemeral(true).queue();
                return;
            }
        }
        if(planRepository.existsPlanByPollIdAndDateIsNull(event.getTarget().getIdLong())){
            event.reply("This poll is already being used for a plan").setEphemeral(true).queue();
            return;
        }
        TextInput notes = TextInput.create("notes", "Notes about the event", TextInputStyle.SHORT)
                .setPlaceholder("Grinding the event").setRequired(false).build();
        TextInput name = TextInput.create("title", "Title of the event", TextInputStyle.SHORT)
                .setPlaceholder("Hunt Showdown").build();
        TextInput count = TextInput.create("count", "Number of people (not including yourself)", TextInputStyle.SHORT)
                .setPlaceholder("Leave empty for infinite").setRequired(false).build();
        TextInput pollInput = TextInput.create("poll", "Poll Id", TextInputStyle.SHORT)
                .setValue(event.getTarget().getChannelId() + "-" + event.getTarget().getId()).setRequired(true).build();
        event.replyModal(Modal.create("plan_event_modal_poll", "Details of meeting")
                        .addActionRow(name)
                        .addActionRow(notes)
                        .addActionRow(count)
                        .addActionRow(pollInput)
                        .build())
                .queue();
    }

    @DiscordMapping
    private void pollVoted(MessagePollVoteAddEvent event){
        long pollId = event.getMessageIdLong();
        long pollOptionId = event.getAnswerId();
        long userId = event.getUserIdLong();
        if(!planRepository.existsPlanByPollIdAndDateIsNull(pollId)) return;
        PollVotes vote = new PollVotes(pollId, pollOptionId, userId);
        pollVotesRepository.save(vote);
    }

    @DiscordMapping
    private void pollUnVoted(MessagePollVoteRemoveEvent event){
        long pollId = event.getMessageIdLong();
        long pollOptionId = event.getAnswerId();
        long userId = event.getUserIdLong();
        if(!planRepository.existsPlanByPollIdAndDateIsNull(pollId)) return;
        PollVotes vote = new PollVotes(pollId, pollOptionId, userId);
        pollVotesRepository.delete(vote);
    }

    @DiscordMapping
    private void pollEnded(MessageUpdateEvent event){
        MessagePoll poll = event.getMessage().getPoll();
        if(poll == null || !poll.isFinalizedVotes()) return;
        Optional<Plan> optionalPlan = planRepository.findPlanByPollIdAndDateIsNull(event.getMessageIdLong());
        if(optionalPlan.isEmpty()){ return; }
        Plan plan = optionalPlan.get();
        int maxVotes = poll.getAnswers().stream()
                .mapToInt(MessagePoll.Answer::getVotes)
                .max()
                .orElse(0);

        MessagePoll.Answer topAnswer = poll.getAnswers().stream()
                .filter(answer -> answer.getVotes() == maxVotes).findFirst().get();

        Date date = stringToDate(topAnswer.getText());
        plan.setDate(date);
        // Accept all the users who voted for this time
        PlanEvent[] acceptEvents = pollVotesRepository.findAllByPollIdAndOptionId(event.getMessage().getIdLong(), topAnswer.getId())
                .stream()
                .map(PollVotes::getUserId)
                .filter(id -> plan.getInvitees().containsKey(id))
                .map(id -> new PlanEvent(PlanEvent.Event.USER_ACCEPTED, id))
                .toArray(PlanEvent[]::new);
        plan.processEvents(acceptEvents);
        planRepository.save(plan);
        planService.updateMessages(plan);
        String dateMessage = plan.getTitle() + " poll has ended and has been scheduled for " + TimeFormat.DATE_TIME_SHORT.format(plan.getDate().getTime());
        plan.getAcceptedIdsAndAuthor().forEach(id -> planService.sendMessage(plan, id, dateMessage));
    }

    @DiscordMapping(Id = "plan", SubId = "link", FocusedOption = "name")
    private void planLinkNameAutocomplete(
            CommandAutoCompleteInteractionEvent event,
            @EventProperty String name
    ){
        event.replyChoices(
            planRepository.findAllPlansByDateAfterAndNotDeleted(new Date()).stream()
                    .filter(plan -> name.isEmpty() || plan.getTitle().contains(name))
                    .map(plan -> new Command.Choice(plan.getTitle(), plan.getId())).toList()
        ).queue();
    }

    @DiscordMapping(Id = "plan", SubId = "link", FocusedOption = "id")
    private void planLinkIdAutocomplete(
            CommandAutoCompleteInteractionEvent event,
            @EventProperty String id
    ){
        event.replyChoices(
                planRepository.findAllPlansByDateAfterAndNotDeleted(new Date()).stream()
                        .filter(plan -> {
                            String idString = plan.getId() + "";
                            return id.isEmpty() || idString.contains(id);
                        })
                        .map(plan -> new Command.Choice(plan.getId() + "", plan.getId())).toList()
        ).queue();
    }

    @DiscordMapping(Id = "plan", SubId = "link")
    private void planLinkSlashCommand(
            SlashCommandInteractionEvent event,
            @EventProperty String name,
            @EventProperty String id
    ){
        dataOtterService.sendRock(new SlashCommandRock(event));
        long planId = -1;
        try {
            if(name != null) {
                planId = Long.parseLong(name);
            } else {
                planId = Long.parseLong(id);
            }
        } catch (Exception e) {
            event.reply("Unable to convert the id to a long. What happened?").setEphemeral(true).queue();
        }
        event.deferReply(true).queue();
        planRepository.findById(planId).ifPresentOrElse(plan -> {
            Message message = event.getChannel().sendMessageEmbeds(getPlanChannelMessage(plan, event.getGuild())).complete();
            plan.addLinkedMessage(message.getChannelIdLong(), message.getIdLong());
            planRepository.save(plan);
            event.getHook().sendMessage("Plan message added to channel").setEphemeral(true).queue();
        }, () -> event.reply("A plan with that ID does not exist.").setEphemeral(true).queue());
    }

    @DiscordMapping(Id = "plan", SubId = "notifications")
    private void planNotifications(
            SlashCommandInteractionEvent event,
            @EventProperty boolean receive
    ) {
        dataOtterService.sendRock(new SlashCommandRock(event));
        User user = userDataRepository.findById(event.getUser().getIdLong())
                .orElseGet(() -> new User(event.getUser().getIdLong()));
        user.setHour_message(receive);
        userDataRepository.save(user);
        event.reply("Preferences have been updated.").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "plan", SubId = "help")
    private void planHelpCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.replyEmbeds(EmbedMessageGenerator.plannerHelperMessage()).setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "plan", SubId = "event")
    private void planEventSlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
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

    @DiscordMapping(Id = "plan_event_modal_poll")
    private void planEventModalPollCommand(
            ModalInteractionEvent event,
            @EventProperty PlanModalDataDateless planData
    ){
        int count;
        try {
            count = planData.count() != null && !planData.count().isEmpty() ? Integer.parseInt(planData.count()) : -1;
        } catch (NumberFormatException e){
            event.reply("Invalid count").setEphemeral(true).queue();
            return;
        }
        if(count < 1 && count != -1){
            event.reply("Invalid count").setEphemeral(true).queue();
            return;
        }
        long pollId = planData.getPollId();
        long pollChannelId = planData.getPollChannelId();
        event.replyEmbeds(getPrePlanMessage(planData.title(), planData.notes(), count, pollChannelId, pollId))
                .setEphemeral(true)
                .addActionRow(
                        EntitySelectMenu.create("People_poll", EntitySelectMenu.SelectTarget.USER, EntitySelectMenu.SelectTarget.ROLE)
                                .setMinValues(1)
                                .setMaxValues(25)
                                .build())
                .queue();
    }

    @DiscordMapping(Id = "plan_event_modal")
    private void planEventModalResponse(
            ModalInteractionEvent event,
            @EventProperty PlanModalData planData
    ){
        dataOtterService.sendRock(new ModalCommandRock(event));
        Date date = stringToDate(planData.date());
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
            count = planData.count() != null && !planData.count().isEmpty() ? Integer.parseInt(planData.count()) : -1;
        } catch (NumberFormatException e){
            event.reply("Invalid count").setEphemeral(true).queue();
            return;
        }
        if(count < 1 && count != -1){
            event.reply("Invalid count").setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(getPrePlanMessage(planData.title(), planData.notes(), count, planData.date()))
                .setEphemeral(true)
                .addActionRow(
                        EntitySelectMenu.create("People", EntitySelectMenu.SelectTarget.USER, EntitySelectMenu.SelectTarget.ROLE)
                                .setMinValues(1)
                                .setMaxValues(25)
                                .build())
                .queue();
    }

    @DiscordMapping(Id = "People_poll")
    private void planPeopleResponsePoll(EntitySelectInteractionEvent event){
        if(event.getMentions().getMembers().isEmpty() && event.getMentions().getRoles().isEmpty()) {
            event.reply("You must select a member or role to invite to the event").setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).queue();
        List<MessageEmbed.Field> fields = event.getMessage().getEmbeds().get(0).getFields();
        String title = fields.stream().filter(field -> field.getName().equals("title")).findFirst().get().getValue();
        String notes = fields.stream().filter(field -> field.getName().equals("notes")).findFirst().get().getValue().replace((char)8206 + "", "");
        String[] pollAndChannel = fields.stream().filter(field -> field.getName().equals("poll")).findFirst().get().getValue().split("-");
        long poll = Long.parseLong(pollAndChannel[1]);
        long pollChannel = Long.parseLong(pollAndChannel[0]);
        int count = Integer.parseInt(fields.stream().filter(field -> field.getName().equals("count")).findFirst().get().getValue());
        PlanCreationData planData = new PlanCreationData(
                title,
                notes,
                null,
                event.getUser().getIdLong(),
                event.getMentions().getMembers().stream().map(ISnowflake::getIdLong).toList(),
                event.getMentions().getRoles().stream().map(ISnowflake::getIdLong).toList(),
                count,
                poll,
                pollChannel
        );
        Plan plan = planService.createPlan(planData);
        if(plan == null) {
            event.getHook().setEphemeral(true).sendMessage("Event not created as the invite list resolves to empty. Invites to yourself, bots or users who are marked with `no plan` do not count.").queue();
            return;
        }
        planService.sendPollToAll(plan);
        event.getHook().setEphemeral(true).sendMessage("Event created in <#" + discordPlanId + ">").queue();
        event.getMessage().delete().queue();
    }

    @DiscordMapping(Id = "People")
    private void planPeopleResponse(EntitySelectInteractionEvent event){
        if(event.getMentions().getMembers().isEmpty() && event.getMentions().getRoles().isEmpty()) {
            event.reply("You must select a member or role to invite to the event").setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).queue();
        List<MessageEmbed.Field> fields = event.getMessage().getEmbeds().get(0).getFields();
        String title = fields.stream().filter(field -> field.getName().equals("title")).findFirst().get().getValue();
        String notes = fields.stream().filter(field -> field.getName().equals("notes")).findFirst().get().getValue().replace((char)8206 + "", "");
        Date date = stringToDate(fields.stream().filter(field -> field.getName().equals("date")).findFirst().get().getValue());
        int count = Integer.parseInt(fields.stream().filter(field -> field.getName().equals("count")).findFirst().get().getValue());
        PlanCreationData planData = new PlanCreationData(
                title,
                notes,
                date,
                event.getUser().getIdLong(),
                event.getMentions().getMembers().stream().map(ISnowflake::getIdLong).toList(),
                event.getMentions().getRoles().stream().map(ISnowflake::getIdLong).toList(),
                count,
                null,
                null
        );
        boolean success = planService.createPlan(planData) != null;
        if(!success) {
            event.getHook().setEphemeral(true).sendMessage("Event not created as the invite list resolves to empty. Invites to yourself, bots or users who are marked with `no plan` do not count.").queue();
            return;
        }
        event.getHook().setEphemeral(true).sendMessage("Event created in <#" + discordPlanId + ">").queue();
        event.getMessage().delete().queue();
    }

    @DiscordMapping(Id = "edit_event_modal")
    private void editEventModal(ModalInteractionEvent event){
        dataOtterService.sendRock(new ModalCommandRock(event));
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        String notes = event.getValue("notes").getAsString();
        String dateString = event.getValue("date").getAsString();
        String title = event.getValue("title").getAsString();
        Date date = stringToDate(dateString);
        if(date == null && !dateString.equals("poll")){
            event.reply(STD_HELPER_MESSAGE).setEphemeral(true).queue();
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
        if(date != null){
            plan.setDate(date);
        }
        planService.updateMessages(plan);
        planRepository.save(plan);
        event.reply("Plan details have been edited").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "add_users")
    private void addUsersButton(ButtonInteractionEvent event){
        dataOtterService.sendRock(new ButtonCommandRock(event));
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        if(plan.getAuthorId() != event.getUser().getIdLong()){
            event.reply("You are not the owner of this event").setEphemeral(true).queue();
            return;
        }
        event.reply("Select people to add to the event. Plan id:" + plan.getId()).setActionRow(
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
        Long[] ids = event.getMentions().getMembers().stream().map(ISnowflake::getIdLong).toList().toArray(Long[]::new);
        planService.addUsersToPlan(plan, ids);
        event.getHook().sendMessage("Added user(s) to event").setEphemeral(true).queue();
        event.getMessage().delete().queue();
    }

    @DiscordMapping(Id = "send_message_modal")
    private void sendMessageModal(
            ModalInteractionEvent event,
            @EventProperty String message
    ){
        event.deferReply().queue();
        dataOtterService.sendRock(new ModalCommandRock(event));
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        planService.sendMessage(plan, event.getUser().getIdLong(), message);
        event.getHook().sendMessage("Message sent to all accepted people").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "send_message")
    private void sendMessageEvent(ButtonInteractionEvent event){
        dataOtterService.sendRock(new ButtonCommandRock(event));
        TextInput message = TextInput.create("message", "Message to be sent to accepted users", TextInputStyle.PARAGRAPH).build();
        event.replyModal(Modal.create("send_message_modal", "Send message").addActionRow(message) .build()) .queue();
    }

    @DiscordMapping(Id = "edit_event")
    private void editDetailsButtonEvent(ButtonInteractionEvent event){
        dataOtterService.sendRock(new ButtonCommandRock(event));
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
            dateString = "poll";
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
        dataOtterService.sendRock(new ButtonCommandRock(event));
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        planService.deletePlan(plan);
    }

    @DiscordMapping(Id = "request_fill_in")
    private void requestFillIn(ButtonInteractionEvent event){
        event.deferEdit().queue();
        dataOtterService.sendRock(new ButtonCommandRock(event));
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.requestFillIn(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "fill_in")
    private void fillIn(ButtonInteractionEvent event){
        event.deferEdit().queue();
        dataOtterService.sendRock(new ButtonCommandRock(event));
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.fillIn(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "drop_out_event")
    private void dropOutEvent(ButtonInteractionEvent event){
        dataOtterService.sendRock(new ButtonCommandRock(event));
        event.editButton(Button.danger("confirm_drop_out_event", "Confirm Dropout")).queue();
    }

    @DiscordMapping(Id = "confirm_drop_out_event")
    private void confirmDropOutEvent(ButtonInteractionEvent event){
        event.deferEdit().queue();
        dataOtterService.sendRock(new ButtonCommandRock(event));
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.dropOut(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "accept_event")
    private void acceptEvent(ButtonInteractionEvent event){
        event.deferEdit().queue();
        dataOtterService.sendRock(new ButtonCommandRock(event));
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.accept(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "waitlist_event")
    private void waitlistEvent(ButtonInteractionEvent event){
        event.deferEdit().queue();
        dataOtterService.sendRock(new ButtonCommandRock(event));
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.waitList(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "deny_event")
    private void denyEvent(ButtonInteractionEvent event){
        event.deferEdit().queue();
        dataOtterService.sendRock(new ButtonCommandRock(event));
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.deny(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "maybe_event")
    private void maybeEvent(ButtonInteractionEvent event) {
        event.deferEdit().queue();
        dataOtterService.sendRock(new ButtonCommandRock(event));
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.maybe(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "schedule_reminder")
    private void scheduleReminder(ButtonInteractionEvent event){
        TextInput message = TextInput.create("message", "Message", TextInputStyle.SHORT).setRequired(true).build();
        TextInput date = TextInput.create("date", "Date", TextInputStyle.SHORT).setRequired(true).build();

        event.replyModal(Modal.create("schedule_reminder_modal", "Schedule Reminder")
            .addActionRow(message)
            .addActionRow(date)
            .build()
        ).queue();
    }

    @DiscordMapping(Id = "schedule_reminder_modal")
    private void scheduleReminderModal(
        ModalInteractionEvent event,
        @EventProperty(name = "date") String dateString,
        @EventProperty String message
    ){
        Date date = stringToDate(dateString);
        if(date == null){
            event.reply("Date is invalid").setEphemeral(true).queue();
            return;
        }
        Plan plan = planRepository.findById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText())).get();
        planService.createPlanReminder(plan, date, PlanUser.Status.MAYBED, message);
        event.reply("Reminder has been scheduled").setEphemeral(true).queue();
    }

    @Scheduled(cron = "0 * * * * *")
    private void minuteTasks(){
        Instant time = Instant.now().plus(5, ChronoUnit.MINUTES);
        Date startTime = new Date();
        Date endTime = Date.from(time);
        planService.getConnectedPartyGoers().forEach((userId, channel) ->
                planRepository.findAllPlansByAuthorIdBetweenDates(userId, startTime, endTime).forEach(plan -> {
                    if(linkedMessageRepository.existsById_ChannelId(channel.getIdLong())) return;
                    Message message = channel.sendMessageEmbeds(getPlanChannelMessage(plan, channel.getGuild())).complete();
                    plan.addLinkedMessage(channel.getIdLong(), message.getIdLong());
                    planRepository.save(plan);
        }));
    }
}
