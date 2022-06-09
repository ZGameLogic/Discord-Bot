package bot.role;

import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.helpers.roleData.Role;
import bot.role.helpers.roleData.RoleDataRepository;
import data.serializing.DataCacher;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public abstract class RoleBotReady {

    private static  Logger logger = LoggerFactory.getLogger(RoleBotReady.class);

    public static List<SlashCommandData> getCommands(){
        List<SlashCommandData> commands = new LinkedList<>();
        // Role bot listener
        commands.add(Commands.slash("stats", "Posts the players stats in chat")
                .addOption(OptionType.USER, "player", "Player's stats to see", false));
        commands.add(Commands.slash("challenge", "Challenges a player for their role. A win switches the roles!")
                .addOption(OptionType.USER, "player", "The player you wish to challenge", true));
        commands.add(Commands.slash("role-stats", "Lists everyone in the caste level and their stats if they can still defend for the day")
                .addOption(OptionType.ROLE, "role", "Role to see the stats of", true)
                .addOption(OptionType.BOOLEAN, "include-all", "Whether or not to include the people who have already defended today", false));
        commands.add(Commands.slash("leaderboard", "Get the top 10 players in a specific category")
                .addSubcommands(new SubcommandData("strength", "Shows the strength statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("knowledge", "Shows the knowledge statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("magic", "Shows the magic statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("agility", "Shows the agility statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("stamina", "Shows the stamina statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("gold", "Shows the richest citizens"))
                .addSubcommands(new SubcommandData("total", "Shows the citizens with the most stats"))
                .addSubcommands(new SubcommandData("wins", "Shows the citizens with the most wins"))
                .addSubcommands(new SubcommandData("losses", "Shows the citizens with the most losses"))
                .addSubcommands(new SubcommandData("castes", "Shows the population of each castes"))
                .addSubcommands(new SubcommandData("activities", "Shows a list of active members who still have not taken their activities for today"))
        );
        commands.add(Commands.slash("remind", "Have shlongbot message you an hour before a day is done")
                .addOption(OptionType.BOOLEAN, "should-remind", "True if you want a reminder, false if you don't", false));
        commands.add(Commands.slash("manage-inventory", "Change what slot an item is in")
                .addOption(OptionType.INTEGER, "slot-one", "The first slot to swap", true)
                .addOption(OptionType.INTEGER, "slot-two", "The second slot to swap", true));
        commands.add(Commands.slash("pay-citizen", "Gives your gold to a citizen of your choice")
                .addOption(OptionType.USER, "citizen", "The citizen to receive your gold", true)
                .addOption(OptionType.INTEGER, "gold", "The amount of gold to give", true));
        commands.add(Commands.slash("pray", "Pray to Shlongbot"));
        commands.add(Commands.slash("fight-stats", "View a more detailed breakdown of a fight between players")
                .addOption(OptionType.STRING, "id", "id of the fight to get more details on", true));

        // Role bot king
        commands.add(Commands.slash("distribute-wealth", "Gives some of your wealth to a caste system")
                .addOption(OptionType.ROLE, "role", "The caste level of where you want your gold to go", true)
                .addOption(OptionType.INTEGER, "gold", "The amount of gold to distribute", true));

        commands.add(Commands.slash("propose-tax", "Forces a caste to pay a tax at the start of the next day")
                .addOption(OptionType.ROLE, "role", "The caste level to tax", true)
                .addOption(OptionType.INTEGER, "gold", "The amount of gold to tax", true));

        commands.add(Commands.slash("honorable-promotion", "Forces two citizens to switch roles. Used once per day")
                .addOption(OptionType.USER, "citizen-one", "One of the two citizens to switch roles", true)
                .addOption(OptionType.USER, "citizen-two", "One of the two citizens to switch roles", true));
        commands.add(Commands.slash("pass-law", "Create a law for the kingdom to follow from now on!")
                .addOption(OptionType.STRING, "law", "Law to be added", true));

        return commands;
    }

    public static void checkGuild(Guild guild){
        try {
            emojiChecking(guild);
            casteRoleChecking(guild);
            channelChecking(guild);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void channelChecking(Guild guild) {
        DataCacher<GameConfigValues> gameData = new DataCacher<>("arena\\game config data");
        GameConfigValues gcv = gameData.loadSerialized();
        logger.info("Searching for categories...");
        Category war = null;
        Category caste = null;
        for(Category category : guild.getCategories()){
            if(category.getName().toLowerCase().contains("war")){
                logger.info("Found war category");
                war = category;
            } else if(category.getName().toLowerCase().contains("caste system chat")){
                logger.info("Found caste category");
                caste = category;
            }
        }

        if(war == null){
            war = guild.createCategory("——————War———————").complete();
            logger.info("Added war category");
        }
        if(caste == null){
            caste = guild.createCategory("——Caste system Chat——").complete();
            logger.info("Added caste category");
        }
        gcv.setWarCategoryId(war.getIdLong());

        logger.info("Searching for channels...");
        for(Role role : RoleDataRepository.getRoles()){
            List<TextChannel> channels = guild.getTextChannelsByName(role.getPlural(), true);
            if(channels.size() > 0){
                logger.info("Found channel : " + role.getPlural());
            } else {
                TextChannel casteChannel = guild.createTextChannel(role.getPlural(), caste).complete();
                long id = gcv.getRoleIds().get(role.getShortName());
                casteChannel.getManager().putRolePermissionOverride(
                        id,
                        new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL})),
                        new LinkedList<> ()
                ).putRolePermissionOverride(
                        guild.getPublicRole().getIdLong(),
                        new LinkedList<> (),
                        new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL}))
                ).queue();
                logger.info("Added channel: " + role.getPlural());
            }
        }
        List<String> extraChannels = new LinkedList<>(Arrays.asList(new String[]{"war", "activities", "encounters", "items"}));
        Map<String, Long> channelIds = new HashMap<>();
        for(String channelName : extraChannels){
            boolean found = false;
            for(TextChannel discordChannel : guild.getTextChannels()){
                if(discordChannel.getName().toLowerCase().equals(channelName)){
                    found = true;
                    logger.info("Found channel : " + channelName);
                    channelIds.put(channelName, discordChannel.getIdLong());
                }
            }
            if(!found){
                TextChannel createdChannel = guild.createTextChannel(channelName, war).complete();
                channelIds.put(channelName, createdChannel.getIdLong());
                if(!channelName.equals("war")){
                    createdChannel.getManager().putRolePermissionOverride(
                            guild.getPublicRole().getIdLong(),
                            new LinkedList<> (),
                            new LinkedList<> (Arrays.asList(new Permission[] {Permission.MESSAGE_SEND}))
                    ).queue();
                }
                logger.info("Added channel: " + channelName);
            }
        }
        gcv.setChannelIds(channelIds);
        gameData.saveSerialized(gcv);
    }


    private static void emojiChecking(Guild guild) throws IOException {
        logger.info("Searching for emotes...");
        DataCacher<GameConfigValues> gameData = new DataCacher<>("arena\\game config data");
        GameConfigValues gcv = gameData.loadSerialized();
        File emoteDir = new ClassPathResource("Role bot\\Emotes").getFile();

        Map<String, Long> newVals = new HashMap<>();
        for(File f : emoteDir.listFiles()){
            String emoteName = f.getName().replace(".png", "");
            boolean foundOld = false;
            for(Emote e : guild.getEmotes()){ // look for emote in server
                if(e.getName().equals(emoteName)){
                    foundOld = true;
                    newVals.put(emoteName, e.getIdLong());
                    logger.info("Found emote: " + emoteName);
                    break;
                }
            }
            if(!foundOld){ // if we didn't find the emote in the server
                Icon icon = Icon.from(f);
                Emote uploaded = guild.createEmote(emoteName, icon).complete();
                newVals.put(uploaded.getName(), uploaded.getIdLong());
                logger.info("Added emote: " + emoteName);
            }
        }
        gcv.setIconIds(newVals);
        gameData.saveSerialized(gcv);
    }

    private static void casteRoleChecking(Guild guild) throws IOException {
        logger.info("Checking for caste roles...");
        DataCacher<GameConfigValues> gameData = new DataCacher<>("arena\\game config data");
        GameConfigValues gcv = gameData.loadSerialized();
        Map<String, Icon> icons = new HashMap<>();
        for(File f :new ClassPathResource("Role bot\\Emotes").getFile().listFiles()){
            icons.put(f.getName().replace(".png", ""), Icon.from(f));
        }
        Map<String, Long> newVals = new HashMap<>();
        for(Role role : RoleDataRepository.getRoles()){
            boolean foundOld = false;
            for(net.dv8tion.jda.api.entities.Role guildRole : guild.getRoles()){
                if(guildRole.getName().equals(role.getLongName())){
                    logger.info("Found caste role: " + role.getShortName());
                    foundOld = true;
                    newVals.put(role.getShortName(), guildRole.getIdLong());
                    break;
                }
            }
            if(!foundOld){
                net.dv8tion.jda.api.entities.Role guildRole = guild.createRole().setName(role.getLongName()).setHoisted(true).setColor(role.getColor()).complete();
                int boostLevel = guild.getBoostTier().getKey();
                if(boostLevel >= 2) {
                    guildRole.getManager().setIcon(icons.get(role.getShortName())).queue();
                }
                logger.info("Added caste role: " + role.getShortName());
                newVals.put(role.getShortName(), guildRole.getIdLong());
            }
        }
        // TODO order roles
        //guild.modifyRolePositions()
        gcv.setRoleIds(newVals);
        gameData.saveSerialized(gcv);
    }
}
