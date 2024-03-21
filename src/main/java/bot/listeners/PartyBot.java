package bot.listeners;

import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
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
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Party bot stuff
 */
@DiscordController
@Slf4j
public class PartyBot  {

    private final GuildDataRepository guildData;
    private final HashMap<String, LinkedList<String>> chatroomNames;

    @Bot
    private JDA bot;

    @Autowired
    public PartyBot(GuildDataRepository guildData){
        chatroomNames = new HashMap<>();
        this.guildData = guildData;
        LinkedList<String> minecraft = new LinkedList<>();
        minecraft.add("Plains");
        minecraft.add("Forest");
        minecraft.add("Birch Forest");
        minecraft.add("Dark Forest");
        minecraft.add("Jungle");
        minecraft.add("Taiga");
        minecraft.add("Snowy Tundra");
        minecraft.add("Snowy Taiga");
        minecraft.add("Savanna");
        minecraft.add("Desert");
        minecraft.add("Mesa");
        minecraft.add("Badlands");
        minecraft.add("Swamp");
        minecraft.add("Mushroom Fields");
        minecraft.add("Ocean");
        minecraft.add("Deep Ocean");
        minecraft.add("Cold Ocean");
        minecraft.add("Frozen Ocean");
        minecraft.add("Mountain");
        minecraft.add("Wooded Mountain");
        minecraft.add("Basalt Deltas");
        minecraft.add("Crimson Forest");
        minecraft.add("Warped Forest");
        minecraft.add("Nether Wastes");
        minecraft.add("Soul Sand Valley");
        minecraft.add("End Highlands");
        minecraft.add("End Midlands");
        minecraft.add("End Barrens");
        minecraft.add("The End");
        chatroomNames.put("Minecraft", minecraft);
        LinkedList<String> sot = new LinkedList<>();
        sot.add("Ancient Spire Outpost");
        sot.add("Cannon Cove");
        sot.add("Chicken Isle");
        sot.add("Crooks Hollow");
        sot.add("Devils Ridge");
        sot.add("Discovery Ridge");
        sot.add("Fools Lagoon");
        sot.add("Golden Sands Outpost");
        sot.add("Krakens Fall");
        sot.add("Lone Cove");
        sot.add("Lookout Point");
        sot.add("Marauders Arch");
        sot.add("Mermaids Hideaway");
        sot.add("Molten Sands Fortress");
        sot.add("Old Faithful Isle");
        sot.add("Plunder Outpost");
        sot.add("Plunder Valley");
        sot.add("Sanctuary Outpost");
        sot.add("Shipwreck Bay");
        sot.add("Smugglers Bay");
        sot.add("Snake Island");
        sot.add("The Crooked Masts");
        sot.add("The Sunken Grove");
        sot.add("Thieves Haven");
        sot.add("Wanderers Refuge");
        chatroomNames.put("Sea of Thieves", sot);
        LinkedList<String> general = new LinkedList<>();
        general.add("Chatroom");
        general.add("Hangout");
        general.add("Chillin");
        general.add("Roomchat");
        general.add("Compound");
        general.add("Cell Block");
        general.add("Wooded Cabin");
        general.add("Wooden Cabin");
        general.add("Celestial Cloud");
        general.add("Back Alley");
        general.add("Windswept Mountain");
        general.add("Moonlit Oasis");
        general.add("Secret Garden");
        general.add("Neon Nights");
        general.add("Starship Lounge");
        general.add("Enchanted Forest");
        general.add("Pixel Palace");
        general.add("Galactic Hub");
        general.add("Sunset Haven");
        general.add("Sapphire Springs");
        general.add("Echoing Caverns");
        general.add("Serenity Cove");
        general.add("Whispering Pines");
        general.add("Crystal Clear");
        general.add("Aurora Borealis");
        general.add("Mystic Manor");
        general.add("Eternal Flame");
        general.add("Tranquil Waters");
        general.add("Twilight Terrace");
        general.add("Ocean's Horizon");
        general.add("Mystic Meadows");
        general.add("Desert Mirage");
        general.add("Velvet Valley");
        general.add("Cosmic Crossing");
        general.add("Polar Paradise");
        general.add("Hidden Hollow");
        general.add("Thundering Tundra");
        general.add("Shimmering Shore");
        general.add("Luminous Lagoon");
        general.add("Dreamy Dale");
        general.add("Frosty Fjord");
        general.add("Skyline Sanctuary");
        general.add("Vibrant Vista");
        general.add("Ember Enclave");
        general.add("Silver Sands");
        general.add("Dazzling Depths");
        general.add("Midnight Meadow");
        general.add("Sunset Shore");
        general.add("Mountain Retreat");
        general.add("Ocean View");
        general.add("Forest Hideaway");
        general.add("River Bend");
        general.add("Seaside Escape");
        general.add("Desert Oasis");
        general.add("Lakeside Lodge");
        general.add("Sky High");
        general.add("Arctic Edge");
        general.add("Tropical Paradise");
        general.add("Urban Jungle");
        general.add("Countryside Corner");
        general.add("Deep Blue");
        general.add("Golden Fields");
        general.add("Mystical Ruins");
        general.add("Northern Lights");
        general.add("Sandy Beach");
        general.add("Rainforest Canopy");
        general.add("Volcanic Valley");
        chatroomNames.put("general", general);
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
        new Thread(() -> {
            guildData.findByChatroomEnabledTrue().forEach(data ->
                    checkCreateChatroom(event.getJDA().getGuildById(data.getId()))
            );
        }, "PartyBot Startup").start();
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
            int number = 1;
            List<String> chatroomNames = this.chatroomNames.get("general");
            String chatroomName = chatroomNames.get((int) (Math.random() * chatroomNames.size()));
            while (!guild.getVoiceChannelsByName(chatroomName + " " + number, true).isEmpty()) {
                number++;
            }
            VoiceChannel newChannel = guild.createVoiceChannel(chatroomName + " " + number)
                    .setParent(guild.getCategoryById(savedGuild.getPartyCategory())).complete();
            newChannel.modifyStatus("general").queue();
            for (Member member : members) {
                guild.moveVoiceMember(member, newChannel).queue();
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void renameChatroom(){
        guildData.findByChatroomEnabledTrue().forEach(guild -> {
            long partyCatId = guild.getPartyCategory();
            long guildId = guild.getId();
            long afkChannelId = guild.getAfkChannelId();
            long createChannelId = guild.getCreateChatId();
            bot.getGuildById(guildId).getCategoryById(partyCatId)
                .getVoiceChannels()
                .stream()
                .filter(channel -> channel.getIdLong() != afkChannelId && channel.getIdLong() != createChannelId)
                .forEach(channel -> {
                    String game = getChatroomGame(channel);
                    if(channel.getStatus().equals(game)) return; // if the status is already the game, return
                    channel.modifyStatus(game).queue(); // set the status equal to the game
                    String channelCurrentName = channel.getName().replaceAll("\\d+", "").trim();
                    if(chatroomNames.entrySet().stream().noneMatch(entry -> entry.getValue().contains(channelCurrentName))) return; // if the chatroom name was edited, and is not in the map, return
                    game = chatroomNames.containsKey(game) ? game : "general";
                    if(chatroomNames.get(game).contains(channelCurrentName)) return; // if the chatroom is already in the category it was about to switch to, return
                    List<String> newChatNames = this.chatroomNames.get(this.chatroomNames.containsKey(game) ? game : "general");
                    channel.getManager().setName(newChatNames.get(ThreadLocalRandom.current().nextInt(0, newChatNames.size()))).queue();
            });
        });
    }

    private String getChatroomGame(VoiceChannel channel){
        Map<String, Long> games = channel.getMembers().stream()
                .flatMap(member -> member.getActivities().stream())
                .filter(activity -> activity.getType() == Activity.ActivityType.PLAYING)
                .collect(Collectors.groupingBy(Activity::getName, Collectors.counting()));
        return games.entrySet().stream().min((entry1, entry2) -> {
                    int valueCompare = entry2.getValue().compareTo(entry1.getValue());
                    if (valueCompare == 0) {
                        boolean isFirstKeyInChatroomNames = chatroomNames.containsKey(entry1.getKey());
                        boolean isSecondKeyInChatroomNames = chatroomNames.containsKey(entry2.getKey());
                        if (isFirstKeyInChatroomNames && !isSecondKeyInChatroomNames) {
                            return -1;
                        } else if (!isFirstKeyInChatroomNames && isSecondKeyInChatroomNames) {
                            return 1;
                        }
                        return 0;
                    }
                    return valueCompare;
                })
                .map(Map.Entry::getKey)
                .orElse("general");
    }
}
