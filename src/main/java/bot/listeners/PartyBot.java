package bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import data.database.chatroomNames.ChatroomNamesRepository;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Party bot stuff
 */
@DiscordController
@Slf4j
public class PartyBot  {

    private final GuildDataRepository guildData;
    private final ChatroomNamesRepository namesRepository;

    @Autowired
    public PartyBot(GuildDataRepository guildData, ChatroomNamesRepository namesRepository){
        this.namesRepository = namesRepository;
        this.guildData = guildData;
    }

    @DiscordMapping(Id = "enable_party")
    private void enableParty(ButtonInteractionEvent event){
        event.editButton(Button.success("disable_party", "Party Bot")).queue();
        // edit the discord
        Guild guild = event.getGuild();
        // commands
        long renameChatroomId = guild.upsertCommand(Commands.slash("rename-chatroom", "Renames chatroom to a new name")
                .addOption(OptionType.STRING, "name", "Chatroom name", true)).complete().getIdLong();
        long limitId = guild.upsertCommand(Commands.slash("limit", "Limits the amount of people who can enter a chatroom")
                .addOption(OptionType.INTEGER, "count", "Number of people allowed in the chatroom", true)).complete().getIdLong();
        // voice channel shenanigans
        Category cat = guild.createCategory("Voice channels").complete();
        VoiceChannel afk = guild.getAfkChannel();
        if(afk == null){
            afk = guild.createVoiceChannel("AFK", cat).complete();
            guild.getManager().setAfkChannel(afk).queue();
            guild.getManager().setAfkTimeout(Guild.Timeout.SECONDS_300).queue();
        } else {
            afk.getManager().setParent(cat).queue();
        }
        Channel createChat = guild.createVoiceChannel("Create chatroom", cat).complete();

        // edit the database
        GuildData savedGuild = guildData.findById(event.getGuild().getIdLong()).get();
        savedGuild.setChatroomEnabled(true);
        savedGuild.setPartyCategory(cat.getIdLong());
        savedGuild.setCreateChatId(createChat.getIdLong());
        savedGuild.setAfkChannelId(afk.getIdLong());
        savedGuild.setRenameCommandId(renameChatroomId);
        savedGuild.setLimitCommandId(limitId);
        guildData.save(savedGuild);
    }

    @DiscordMapping(Id = "disable_party")
    private void disableParty(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_party", "Party Bot")).queue();
        // edit the discord
        Guild guild = event.getGuild();
        GuildData savedGuild = guildData.getReferenceById(event.getGuild().getIdLong());
        // commands
        guild.deleteCommandById(savedGuild.getLimitCommandId()).queue();
        guild.deleteCommandById(savedGuild.getRenameCommandId()).queue();
        // voice channel shenanigans
        guild.getVoiceChannelById(savedGuild.getCreateChatId()).delete().queue();
        guild.getCategoryById(savedGuild.getPartyCategory()).delete().queue();

        // edit the database
        savedGuild.setChatroomEnabled(false);
        savedGuild.setPartyCategory(0L);
        savedGuild.setCreateChatId(0L);
        savedGuild.setAfkChannelId(0L);
        savedGuild.setRenameCommandId(0L);
        savedGuild.setLimitCommandId(0L);
        guildData.save(savedGuild);
    }

    @DiscordMapping(Id = "rename-chatroom")
    private void renameChatroom(SlashCommandInteractionEvent event){
        try {
            // get voice channel the user is in
            AudioChannel voice = event.getMember().getVoiceState().getChannel();
            if (event.getGuild().getVoiceChannelById(voice.getIdLong()).getParentCategoryIdLong() == guildData.findById(event.getGuild().getIdLong()).get().getPartyCategory()) {
                try {
                    // rename the channel and set valid command to true
                    voice.getManager().setName(event.getOption("name").getAsString()).queue();
                    event.getGuild().getVoiceChannelById(voice.getIdLong()).modifyStatus("").queue();
                    event.reply("Channel name updated").setEphemeral(true).queue();
                } catch (IllegalArgumentException e1) {
                    event.reply("Channel name not updated").setEphemeral(true).queue();
                }
            } else {
                event.reply("You are not in a voice channel that supports this command").setEphemeral(true).queue();
            }
        } catch (NullPointerException e) {
            event.reply("You are not in a voice channel that supports this command").setEphemeral(true).queue();
        }
    }

    @DiscordMapping(Id = "limit")
    private void limit(SlashCommandInteractionEvent event){
        try {
            // get voice channel the user is in
            AudioChannel voice = event.getMember().getVoiceState().getChannel();
            if (event.getGuild().getVoiceChannelById(voice.getIdLong()).getParentCategoryIdLong() == guildData.findById(event.getGuild().getIdLong()).get().getPartyCategory()) {
                try {
                    int limit = Integer.parseInt(event.getOption("count").getAsString());
                    event.getGuild().getVoiceChannelById(voice.getIdLong()).getManager().setUserLimit(limit).queue();
                    event.reply("Channel limit updated").setEphemeral(true).queue();
                } catch (IllegalArgumentException e1) {
                    event.reply("Channel limit not updated").setEphemeral(true).queue();
                }
            } else {
                event.reply("You are not in a voice channel that supports this command").setEphemeral(true).queue();
            }
        } catch (NullPointerException e) {
            event.reply("You are not in a voice channel that supports this command").setEphemeral(true).queue();
        }
    }

    /**
     * Login event
     */
    @DiscordMapping
    public void ready(ReadyEvent event) {
        new Thread(() -> guildData.findByChatroomEnabledTrue().forEach(data ->
                checkCreateChatroom(event.getJDA().getGuildById(data.getId()))
        ), "PartyBot Startup").start();
    }

    @DiscordMapping
    public void sessionResume(SessionResumeEvent event) {
        for(GuildData data : guildData.findAll()){
            if(data.getChatroomEnabled()){
                checkCreateChatroom(event.getJDA().getGuildById(data.getId()));
            }
        }
    }

    @DiscordMapping
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(guildData.findById(event.getGuild().getIdLong()).get().getChatroomEnabled()) {
            if (event.getChannelJoined() != null && event.getChannelLeft() != null) {
                playerLeft(event.getChannelLeft(), event.getGuild());
                playerJoined(event.getChannelJoined(), event.getGuild());
            } else if (event.getChannelJoined() != null) {
                playerJoined(event.getChannelJoined(), event.getGuild());
            } else if (event.getChannelLeft() != null) {
                playerLeft(event.getChannelLeft(), event.getGuild());
            }
        }
    }

    /**
     * Deletes this channel if there are no players in it, and it matches the chat
     * category
     *
     * @param channelLeft The channel the user left
     * @param guild       the guild that the user left from
     */
    private void playerLeft(AudioChannel channelLeft, Guild guild) {
        GuildData savedGuild = guildData.findById(guild.getIdLong()).get();
        VoiceChannel channel = guild.getVoiceChannelById(channelLeft.getIdLong());
        if (channel.getParentCategoryIdLong() == savedGuild.getPartyCategory()) {
            if (channel.getIdLong() != savedGuild.getCreateChatId() && channel.getIdLong() != savedGuild.getAfkChannelId()) {
                // We get here if the channel left in is the chatroom categories
                if (channel.getMembers().isEmpty()) {
                    channel.delete().queue();
                }
            }
        }
    }

    /**
     * If the user joins the create channel, create a room and move them to it
     *
     * @param channelJoined the channel the user joined
     * @param guild         the guild that this took place in
     */
    private void playerJoined(AudioChannel channelJoined, Guild guild) {
        VoiceChannel channel = guild.getVoiceChannelById(channelJoined.getIdLong());
        GuildData savedGuild = guildData.findById(guild.getIdLong()).get();
        if (channel.getParentCategoryIdLong() == savedGuild.getPartyCategory()) {
            if (channel.getIdLong() == savedGuild.getCreateChatId()) {
                checkCreateChatroom(guild);
            }
        }
    }

    private void checkCreateChatroom(Guild guild) {
        GuildData savedGuild = guildData.findById(guild.getIdLong()).get();
        List<Member> members = guild.getVoiceChannelById(savedGuild.getCreateChatId()).getMembers();
        if (!members.isEmpty()) {
            namesRepository.findRandom().ifPresent(chatroomName -> {
                String name = chatroomName.getName();
                while (!guild.getVoiceChannelsByName(name, true).isEmpty()) {
                    name = chatroomName.getName();
                }
                VoiceChannel newChannel = guild.createVoiceChannel(name)
                        .setParent(guild.getCategoryById(savedGuild.getPartyCategory())).complete();
                newChannel.modifyStatus(chatroomName.getGame()).queue();
                for (Member member : members) {
                    guild.moveVoiceMember(member, newChannel).queue();
                }
            });
        }
    }
}
