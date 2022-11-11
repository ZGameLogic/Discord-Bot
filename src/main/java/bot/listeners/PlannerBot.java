package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

public class PlannerBot extends AdvancedListenerAdapter {

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
        TextInput time = TextInput.create("time", "Time of the event", TextInputStyle.SHORT)
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
        String time = event.getValue("time").getAsString();
        String title = event.getValue("title").getAsString();
        String count = event.getValue("count").getAsString();
        // TODO update database
        event.reply("Select people to invite").setActionRow(
                        EntitySelectMenu.create("People", EntitySelectMenu.SelectTarget.USER)
                                .setMinValues(1)
                                .setMaxValues(10)
                                .build())
                .queue();
    }

    @ButtonResponse(buttonId = "accept_event")
    private void acceptEvent(ButtonInteraction event){
        // TODO clear buttons
        // TODO update database
        // TODO update event modal
    }

    @ButtonResponse(buttonId = "deny_event")
    private void denyEvent(ButtonInteraction event){
        // TODO clear buttons
        // TODO update database
        // TODO update event modal
    }

    @EntitySelectionResponse(menuId = "People")
    private void planPeople(EntitySelectInteraction event){
        event.getMessage().editMessageComponents().queue();
        event.getMessage().editMessage("").queue();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Invites status");
        String desc = "";
        for(Member m : event.getMentions().getMembers()){
            m.getUser().openPrivateChannel().queue(pm -> {
                // TODO send the user a message
            });
            desc += m.getEffectiveName() + ": waiting for reply\n";
        }
        event.replyEmbeds(eb.build()).queue();
    }
}
