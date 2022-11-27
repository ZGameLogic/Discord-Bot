package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
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
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Party bot stuff
 */
public class PartyBot extends AdvancedListenerAdapter {

    private final GuildDataRepository guildData;
    // Linked List of names to hold chatroom names
    private final LinkedList<String> chatroomNames;

    public PartyBot(GuildDataRepository guildData){
        this.guildData = guildData;
        chatroomNames = new LinkedList<>();
        chatroomNames.add("Chatroom");
        chatroomNames.add("Hangout");
        chatroomNames.add("Chillin");
        chatroomNames.add("Roomchat");
    }

    @ButtonResponse("enable_party")
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

    @ButtonResponse("disable_party")
    private void disableParty(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_party", "Party Bot")).queue();
        // edit the discord
        Guild guild = event.getGuild();
        GuildData savedGuild = guildData.getOne(event.getGuild().getIdLong());
        // commands
        guild.deleteCommandById(savedGuild.getLimitCommandId()).queue();
        guild.deleteCommandById(savedGuild.getRenameCommandId()).queue();
        // voice channel shenanigans
        guild.getVoiceChannelById(savedGuild.getCreateChatId()).delete().queue();
        guild.getCategoryById(savedGuild.getPartyCategory()).delete().queue();

        // edit the database
        savedGuild.setChatroomEnabled(false);
        savedGuild.setPartyCategory(0l);
        savedGuild.setCreateChatId(0l);
        savedGuild.setAfkChannelId(0l);
        savedGuild.setRenameCommandId(0l);
        savedGuild.setLimitCommandId(0l);
        guildData.save(savedGuild);
    }

    @SlashResponse("rename-chatroom")
    private void renameChatroom(SlashCommandInteractionEvent event){
        try {
            // get voice channel the user is in
            AudioChannel voice = event.getMember().getVoiceState().getChannel();
            if (event.getGuild().getVoiceChannelById(voice.getIdLong()).getParentCategoryIdLong() == guildData.findById(event.getGuild().getIdLong()).get().getPartyCategory()) {
                try {
                    // rename the channel and set valid command to true
                    voice.getManager().setName(event.getOption("name").getAsString()).queue();
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

    @SlashResponse("limit")
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
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        new Thread(() -> {
            for(GuildData data : guildData.findAll()){
                if(data.getChatroomEnabled()){
                    checkCreateChatroom(event.getJDA().getGuildById(data.getId()));
                }
            }
        }, "PartyBot Startup").start();
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {
        for(GuildData data : guildData.findAll()){
            if(data.getChatroomEnabled()){
                checkCreateChatroom(event.getJDA().getGuildById(data.getId()));
            }
        }
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(guildData.findById(event.getGuild().getIdLong()).get().getChatroomEnabled()) {
            if (event.getChannelJoined() != null && event.getChannelLeft() != null) {
                playerLeft(event.getChannelLeft(), event.getMember(), event.getGuild());
                playerJoined(event.getChannelJoined(), event.getMember(), event.getGuild());
            } else if (event.getChannelJoined() != null) {
                playerJoined(event.getChannelJoined(), event.getMember(), event.getGuild());
            } else if (event.getChannelLeft() != null) {
                playerLeft(event.getChannelLeft(), event.getMember(), event.getGuild());
            }
        }
    }

    /**
     * Deletes this channel if there are no players in it, and it matches the chat
     * category
     *
     * @param channelLeft The channel the user left
     * @param member      the user that left
     * @param guild       the guild that the user left from
     */
    private void playerLeft(AudioChannel channelLeft, Member member, Guild guild) {
        GuildData savedGuild = guildData.findById(guild.getIdLong()).get();
        VoiceChannel channel = guild.getVoiceChannelById(channelLeft.getIdLong());
        if (channel.getParentCategoryIdLong() == savedGuild.getPartyCategory()) {
            if (channel.getIdLong() != savedGuild.getCreateChatId() && channel.getIdLong() != savedGuild.getAfkChannelId()) {
                // We get here if the channel left in is the chatroom categories
                if (channel.getMembers().size() == 0) {
                    channel.delete().queue();
                }
            }
        }
    }

    /**
     * If the user joins the create channel, create a room and move them to it
     *
     * @param channelJoined the channel the user joined
     * @param member        the user
     * @param guild         the guild that this took place in
     */
    private void playerJoined(AudioChannel channelJoined, Member member, Guild guild) {
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
        if (members.size() > 0) {
            int number = 1;
            String chatroomName = chatroomNames.get((int) (Math.random() * chatroomNames.size()));
            while (guild.getVoiceChannelsByName(chatroomName + " " + number, true).size() > 0) {
                number++;
            }
            VoiceChannel newChannel = guild.createVoiceChannel(chatroomName + " " + number)
                    .setParent(guild.getCategoryById(savedGuild.getPartyCategory())).complete();
            for (Member member : members) {
                guild.moveVoiceMember(member, newChannel).queue();
            }
        }
    }
}
