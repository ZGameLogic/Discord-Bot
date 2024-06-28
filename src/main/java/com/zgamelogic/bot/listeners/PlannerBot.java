package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.annotations.EventProperty;
import com.zgamelogic.bot.utils.EmbedMessageGenerator;
import com.zgamelogic.data.database.planData.plan.Plan;
import com.zgamelogic.data.database.planData.plan.PlanRepository;
import com.zgamelogic.data.intermediates.planData.DiscordRoleData;
import com.zgamelogic.data.intermediates.planData.DiscordUserData;
import com.zgamelogic.data.plan.PlanCreationData;
import com.zgamelogic.data.plan.PlanEventResultMessage;
import com.zgamelogic.data.plan.PlanModalData;
import com.zgamelogic.services.PlanService;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.zgamelogic.bot.utils.Helpers.STD_HELPER_MESSAGE;
import static com.zgamelogic.bot.utils.Helpers.stringToDate;
import static com.zgamelogic.bot.utils.PlanHelper.getPrePlanMessage;

@Slf4j
@DiscordController
@RestController
public class PlannerBot {
    @Value("${discord.guild}")
    private long guildId;
    @Value("${discord.plan.id}")
    private long discordPlanId;

    private final PlanRepository planRepository;
    private final PlanService planService;

    @Bot
    private JDA bot;

    public PlannerBot(PlanRepository planRepository, PlanService planService) {
        this.planRepository = planRepository;
        this.planService = planService;
    }

    @Bean
    private List<CommandData> plannerCommands(){
        return List.of(
                Commands.slash("plan", "Plan things")
                        .addSubcommands(new SubcommandData("help", "Description of how this works"))
                        .addSubcommands(new SubcommandData("event", "Plan an event with friends")),
                Commands.slash("text_notifications", "Enable or disable text message notifications")
                        .addSubcommands(
                                new SubcommandData("enable", "Enables text messaging")
                                        .addOption(OptionType.STRING, "number", "your phone number. EX: 16301112222", true),
                                new SubcommandData("disable", "Disables text messaging"))
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

    @DiscordMapping(Id = "plan", SubId = "help")
    private void planHelpCommand(SlashCommandInteractionEvent event){
        event.replyEmbeds(EmbedMessageGenerator.plannerHelperMessage()).setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "plan", SubId = "event")
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

    @DiscordMapping(Id = "plan_event_modal")
    private void planEventModalResponse(
            ModalInteractionEvent event,
            @EventProperty PlanModalData planData
    ){
        Date date = stringToDate(planData.getDate());
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
            count = planData.getCount() != null && !planData.getCount().isEmpty() ? Integer.parseInt(planData.getCount()) : -1;
        } catch (NumberFormatException e){
            event.reply("Invalid count").setEphemeral(true).queue();
            return;
        }
        if(count < 1 && count != -1){
            event.reply("Invalid count").setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(getPrePlanMessage(planData.getTitle(), planData.getNotes(), count, planData.getDate()))
                .setEphemeral(true)
                .addActionRow(
                        EntitySelectMenu.create("People", EntitySelectMenu.SelectTarget.USER, EntitySelectMenu.SelectTarget.ROLE)
                                .setMinValues(1)
                                .setMaxValues(25)
                                .build())
                .queue();
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
                count
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
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        String notes = event.getValue("notes").getAsString();
        String dateString = event.getValue("date").getAsString();
        String title = event.getValue("title").getAsString();
        Date date = stringToDate(dateString);
        if(date == null){
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
        plan.setDate(date);
        planService.updateMessages(plan);
        planRepository.save(plan);
        event.reply("Plan details have been edited").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "add_users")
    private void addUsersButton(ButtonInteractionEvent event){
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
        Plan plan = planRepository.getReferenceById(Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText()));
        planService.sendMessage(plan, message);
        event.getHook().sendMessage("Message sent to all accepted people").setEphemeral(true).queue();
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
        planService.deletePlan(plan);
    }

    @DiscordMapping(Id = "request_fill_in")
    private void requestFillIn(ButtonInteractionEvent event){
        event.deferEdit().queue();
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.requestFillIn(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "fill_in")
    private void fillIn(ButtonInteractionEvent event){
        event.deferEdit().queue();
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.fillIn(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "drop_out_event")
    private void dropOutEvent(ButtonInteractionEvent event){
        event.editButton(Button.danger("confirm_drop_out_event", "Confirm Dropout")).queue();
    }

    @DiscordMapping(Id = "confirm_drop_out_event")
    private void confirmDropOutEvent(ButtonInteractionEvent event){
        event.deferEdit().queue();
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.dropOut(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "accept_event")
    private void acceptEvent(ButtonInteractionEvent event){
        event.deferEdit().queue();
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.accept(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "waitlist_event")
    private void waitlistEvent(ButtonInteractionEvent event){
        event.deferEdit().queue();
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.waitList(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "deny_event")
    private void denyEvent(ButtonInteractionEvent event){
        event.deferEdit().queue();
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.deny(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }

    @DiscordMapping(Id = "maybe_event")
    private void maybeEvent(ButtonInteractionEvent event) {
        event.deferEdit().queue();
        long userId = event.getUser().getIdLong();
        long planId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        PlanEventResultMessage result = planService.maybe(planId, userId);
        if(!result.success()) event.getMessage().reply(result.message()).queue();
    }
}