package bot.listeners;

import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.planData.Plan;
import data.database.planData.PlanRepository;
import data.database.planData.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class PlannerBot extends AdvancedListenerAdapter {

    private PlanRepository planRepository;

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
        TextInput count = TextInput.create("count", "Number of people", TextInputStyle.SHORT)
                .setPlaceholder("3").build();
        event.replyModal(Modal.create("plan_event_modal", "Details of meeting")
                .addActionRow(name)
                .addActionRow(time)
                .addActionRow(count)
                .build()).queue();
    }

    @ModalResponse(modalName = "plan_event_modal")
    private void planEventModalResponse(ModalInteractionEvent event){
        String notes = event.getValue("notes").getAsString();
        String title = event.getValue("title").getAsString();
        int count = Integer.parseInt(event.getValue("count").getAsString());

        event.reply("Select people to invite (Don't include yourself). Plan id:" + event.getIdLong()).setActionRow(
                        EntitySelectMenu.create("People", EntitySelectMenu.SelectTarget.USER)
                                .setMinValues(1)
                                .setMaxValues(10)
                                .build())
                .queue(message -> {
                    Plan plan = new Plan();
                    plan.setTitle(title);
                    plan.setNotes(notes);
                    plan.setAuthorId(event.getUser().getIdLong());
                    plan.setCount(count);
                    plan.setId(event.getIdLong());
                    planRepository.save(plan);
                });
    }

    @ButtonResponse(buttonId = "accept_event")
    private void acceptEvent(ButtonInteraction event){
        // TODO clear buttons for that user
        // TODO update database
        // TODO send message to event organizer
        // TODO update event embed
        // TODO update private message embeds
    }

    @ButtonResponse(buttonId = "deny_event")
    private void denyEvent(ButtonInteraction event){
        // TODO clear buttons for that user
        // TODO update database
        // TODO send message to event organizer
        // TODO update event embed
        // TODO update private message embeds
    }

    @EntitySelectionResponse(menuId = "People")
    private void planPeople(EntitySelectInteraction event){
        long planId = Long.parseLong(event.getMessage().getContentRaw().split(":")[1]);
        event.getMessage().delete().queue();
        HashMap<Long, User> invitees = new HashMap<>();
        for(Member m : event.getMentions().getMembers()){
            invitees.put(m.getIdLong(), new User(m.getIdLong(), 0));
        }
        Plan plan = planRepository.getOne(planId);
        plan.setInvitees(invitees);
        for(Member m : event.getMentions().getMembers()){
            PrivateChannel pm = m.getUser().openPrivateChannel().complete();
            Message message = pm.sendMessageEmbeds(EmbedMessageGenerator.singleInvite(plan, event.getGuild()))
                    .addActionRow(Button.success("accept_event", "Accept"),
                            Button.danger("deny_event", "Deny"))
                    .complete();
            plan.updateMessageIdForUser(m.getIdLong(), message.getIdLong());
        }
        planRepository.save(plan);
        event.replyEmbeds(EmbedMessageGenerator.plan(plan, event.getGuild())).queue();
    }
}
