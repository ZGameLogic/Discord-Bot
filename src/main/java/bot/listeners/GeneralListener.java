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
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
            if(savedGuild.isChatroomEnabled()) {
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
                            .setActionRow(Button.danger("enable_party", "Party Bot"))
                            .queue(message -> {
                                GuildData newGuild = new GuildData();
                                newGuild.setId(guild.getIdLong());
                                newGuild.setConfigChannelId(textChannel.getIdLong());
                                newGuild.setChatroomEnabled(false);
                                newGuild.setConfigMessageId(message.getIdLong());
                                guildData.save(newGuild);
                            });
                });
    }
}
