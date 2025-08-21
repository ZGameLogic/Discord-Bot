package com.zgamelogic.bot.listeners;

import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.DiscordMapping;
import com.zgamelogic.data.database.chatroomNames.ChatroomNamesRepository;
import com.zgamelogic.data.database.guildData.GuildData;
import com.zgamelogic.data.database.guildData.GuildDataRepository;
import com.zgamelogic.data.database.planData.linkedMessage.LinkedMessageRepository;
import com.zgamelogic.data.database.planData.plan.PlanRepository;
import com.zgamelogic.data.intermediates.dataotter.PartyBotRock;
import com.zgamelogic.data.intermediates.dataotter.SlashCommandRock;
import com.zgamelogic.dataotter.DataOtterService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.zgamelogic.bot.utils.PlanHelper.getPlanChannelMessage;

/**
 * Party bot stuff
 */
@DiscordController
@Slf4j
public class PartyBot  {
    private final GuildDataRepository guildData;
    private final ChatroomNamesRepository namesRepository;
    private final LinkedMessageRepository linkedMessageRepository;
    private final PlanRepository planRepository;
    private final DataOtterService dataOtterService;

    private JDA bot;

    @Autowired
    public PartyBot(GuildDataRepository guildData, ChatroomNamesRepository namesRepository, LinkedMessageRepository linkedMessageRepository, PlanRepository planRepository, DataOtterService dataOtterService){
        this.namesRepository = namesRepository;
        this.guildData = guildData;
        this.linkedMessageRepository = linkedMessageRepository;
        this.planRepository = planRepository;
        this.dataOtterService = dataOtterService;
    }

    @DiscordMapping
    private void onReady(ReadyEvent event) {
        bot = event.getJDA();
    }

    @Bean
    private List<CommandData> partyCommands(){
        return List.of(
                Commands.slash("rename-chatroom", "Renames chatroom to a new name")
                        .addOption(OptionType.STRING, "name", "Chatroom name", true),
                Commands.slash("limit", "Limits the amount of people who can enter a chatroom")
                        .addOption(OptionType.INTEGER, "count", "Number of people allowed in the chatroom", true)
        );
    }

    @DiscordMapping(Id = "rename-chatroom")
    private void renameChatroom(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
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
        dataOtterService.sendRock(new SlashCommandRock(event));
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
        checkForEmptyChannels();
    }

    @DiscordMapping
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(guildData.findById(event.getGuild().getIdLong()).get().getChatroomEnabled()) {
            if (event.getChannelJoined() != null && event.getChannelLeft() != null) {
                playerLeft(event.getChannelLeft(), event.getGuild(), event.getMember());
                playerJoined(event.getChannelJoined().asVoiceChannel(), event.getGuild(), event.getMember());
            } else if (event.getChannelJoined() != null) {
                playerJoined(event.getChannelJoined().asVoiceChannel(), event.getGuild(), event.getMember());
            } else if (event.getChannelLeft() != null) {
                playerLeft(event.getChannelLeft(), event.getGuild(), event.getMember());
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void checkForEmptyChannels(){
        for(GuildData data : guildData.findAll()){
            if(!data.getChatroomEnabled()) continue;
            List<Long> ignore = List.of(data.getAfkChannelId(), data.getCreateChatId());
            List<VoiceChannel> channels = bot.getGuildById(data.getId()).getCategoryById(data.getPartyCategory())
                    .getVoiceChannels()
                    .stream()
                    .filter(channel -> !ignore.contains(channel.getIdLong())) // ignore ignored channel
                    .filter(channel -> channel.getMembers().isEmpty()) // only get channels with no members
                    .toList();
            channels.forEach(channel -> channel.delete().queue());
        }
    }

    /**
     * Deletes this channel if there are no players in it, and it matches the chat
     * category
     *
     * @param channelLeft The channel the user left
     * @param guild       the guild that the user left from
     */
    private void playerLeft(AudioChannel channelLeft, Guild guild, Member member) {
        dataOtterService.sendRock(new PartyBotRock(member.getIdLong(), false));
        GuildData savedGuild = guildData.findById(guild.getIdLong()).get();
        VoiceChannel channel = guild.getVoiceChannelById(channelLeft.getIdLong());
        if (channel.getParentCategoryIdLong() == savedGuild.getPartyCategory()) {
            if (channel.getIdLong() != savedGuild.getCreateChatId() && channel.getIdLong() != savedGuild.getAfkChannelId()) {
                // We get here if the channel left in is the chatroom categories
                if (channel.getMembers().isEmpty()) {
                    linkedMessageRepository.deleteAllById_ChannelId(channel.getIdLong());
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
    private void playerJoined(VoiceChannel channelJoined, Guild guild, Member member) {
        VoiceChannel channel = guild.getVoiceChannelById(channelJoined.getIdLong());
        dataOtterService.sendRock(new PartyBotRock(member.getIdLong(), true));
        GuildData savedGuild = guildData.findById(guild.getIdLong()).get();
        if (channel.getParentCategoryIdLong() == savedGuild.getPartyCategory()) {
            if (channel.getIdLong() == savedGuild.getCreateChatId()) {
                checkCreateChatroom(guild);
            } else if(channel.getIdLong() != savedGuild.getAfkChannelId()) {
                Instant time = Instant.now().plus(5, ChronoUnit.MINUTES);
                Date limit = new Date(time.toEpochMilli());
                planRepository.findAllPlansByAuthorIdBetweenDates(member.getIdLong(), new Date(), limit).forEach(plan -> {
                    if(linkedMessageRepository.existsById_ChannelId(channelJoined.getIdLong())) return;
                    Message message = channelJoined.sendMessageEmbeds(getPlanChannelMessage(plan, channelJoined.getGuild())).complete();
                    plan.addLinkedMessage(channelJoined.getIdLong(), message.getIdLong());
                    planRepository.save(plan);
                });
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
                ChannelAction<VoiceChannel> builder = guild.createVoiceChannel(name)
                        .setParent(guild.getCategoryById(savedGuild.getPartyCategory()));
                if(members.stream().map(Member::getIdLong).anyMatch(memberId -> memberId == 232675572772372481L)){
                    builder.addMemberPermissionOverride(195174230281814016L, null, List.of(Permission.VIEW_CHANNEL));
                }
                VoiceChannel newChannel = builder.complete();
                newChannel.sendMessage(String.format("This chatroom name comes from the game: %s", chatroomName.getGame())).queue();
                Instant time = Instant.now().plus(5, ChronoUnit.MINUTES);
                Date limit = new Date(time.toEpochMilli());
                for (Member member : members) {
                    guild.moveVoiceMember(member, newChannel).queue();
                    planRepository.findAllPlansByAuthorIdBetweenDates(member.getIdLong(), new Date(), limit).forEach(plan -> {
                        Message message = newChannel.sendMessageEmbeds(getPlanChannelMessage(plan, newChannel.getGuild())).complete();
                        plan.addLinkedMessage(newChannel.getIdLong(), message.getIdLong());
                        planRepository.save(plan);
                    });
                }
            });
        }
    }
}
