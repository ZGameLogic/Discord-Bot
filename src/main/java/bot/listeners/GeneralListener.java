package bot.listeners;

import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;

public class GeneralListener extends AdvancedListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(GeneralListener.class);
    private final GuildDataRepository guildData;

    public GeneralListener(GuildDataRepository guildData){
        this.guildData = guildData;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        new Thread(()-> {
            //update guilds
            for(Guild guild : event.getJDA().getGuilds()){
                if(!guildData.existsById(guild.getIdLong())){
                    welcomeBot(guild);
                }
            }
            for(GuildData guild: guildData.findAll()){
                Guild g = event.getJDA().getGuildById(guild.getId());
                g.getTextChannelById(guild.getConfigChannelId()).retrieveMessageById(guild.getConfigMessageId()).queue(message -> {
                    LinkedList<LayoutComponent> components = new LinkedList<>();
                    LinkedList<ItemComponent> ics = new LinkedList<>();
                    if(guild.getChatroomEnabled() == null || !guild.getChatroomEnabled()){
                        ics.add(Button.danger("enable_party", "Party Bot"));
                    } else {
                        ics.add(Button.success("disable_party", "Party Bot"));
                    }
                    if(guild.getPlanEnabled() == null || !guild.getPlanEnabled()){
                        ics.add(Button.danger("enable_plan", "Plan bot"));
                    } else {
                        ics.add(Button.success("disable_plan", "Plan bot"));
                    }
                    ActionRow row = ActionRow.of(ics);
                    components.add(row);
                    message.editMessageComponents(components).queue();
                });
            }
        }, "Guild Checking Thread").start();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        welcomeBot(event.getGuild());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        try {
            // edit the discord
            GuildData savedGuild = guildData.findById(event.getGuild().getIdLong()).get();
            if(savedGuild.getChatroomEnabled()) {
                // commands
                guild.deleteCommandById(savedGuild.getLimitCommandId()).queue();
                guild.deleteCommandById(savedGuild.getRenameCommandId()).queue();
                // voice channel shenanigans
                guild.getVoiceChannelById(savedGuild.getCreateChatId()).delete().queue();
                guild.getCategoryById(savedGuild.getPartyCategory()).delete().queue();
            }
            guild.getTextChannelById(savedGuild.getConfigChannelId()).delete().queue();
        } catch (Exception e){
            logger.warn("Unable to delete guild data. Was removed too quickly.");
        }
        guildData.deleteById(event.getGuild().getIdLong());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild() && !event.getAuthor().isBot()){
            event.getJDA().getUserById(232675572772372481l).openPrivateChannel().queue(channel -> {
                channel.sendMessage("Message from " + event.getAuthor().getName() + ":" + event.getAuthor().getId() + "\n" + event.getMessage().getContentRaw())
                        .setActionRow(Button.secondary("reply_message", "Reply")).queue();
            });
        }
    }

    @ButtonResponse("reply_message")
    private void replyMessageButtonPresses(ButtonInteractionEvent event){
        TextInput message = TextInput.create("message", "Reply", TextInputStyle.PARAGRAPH).build();
        event.replyModal(Modal.create("reply_message_modal", "Message response").addActionRow(message).build()).queue();
    }

    @ModalResponse("reply_message_modal")
    private void modalResponseMessageReply(ModalInteractionEvent event){
        event.getJDA().getUserById(
                event.getMessage().getContentRaw().split("\n")[0].split(":")[1]
        ).openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage(event.getValue("message").getAsString()).queue();
        });
        event.reply("Message sent back\n" + event.getValue("message").getAsString()).queue();
    }

    @Override
    public void onUnavailableGuildLeave(UnavailableGuildLeaveEvent event) {
        guildData.deleteById(event.getGuildIdLong());
    }

    private void welcomeBot(Guild guild) {
        guild.createTextChannel("shlongbot")
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(),
                        new LinkedList<>(),
                        new LinkedList<>(Collections.singletonList(Permission.VIEW_CHANNEL)
                        ))
                .setTopic("This is a channel made by shlongbot")
                .queue(textChannel -> {
                    textChannel.sendMessageEmbeds(EmbedMessageGenerator.welcomeMessage(guild.getOwner().getEffectiveName(), guild.getName()))
                            .queue(message -> {
                                GuildData newGuild = new GuildData();
                                newGuild.setId(guild.getIdLong());
                                newGuild.setConfigChannelId(textChannel.getIdLong());
                                newGuild.setChatroomEnabled(false);
                                newGuild.setPlanEnabled(false);
                                newGuild.setConfigMessageId(message.getIdLong());
                                guildData.save(newGuild);
                            });
                });
    }
}
