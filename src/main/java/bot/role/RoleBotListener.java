package bot.role;

import bot.Bot;
import bot.role.data.Data;
import bot.role.data.ResultsData;
import bot.role.data.dungeon.saveable.Dungeon;
import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.data.results.ChallengeFightResults;
import bot.role.data.structures.*;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.structures.Activity;
import bot.role.annotations.ButtonCommand;
import bot.role.annotations.EmoteCommand;
import bot.role.annotations.SlashCommand;
import bot.role.data.structures.item.ShopItem;
import bot.role.helpers.DungeonGenerator;
import controllers.discord.EmbedMessageGenerator;
import data.ConfigLoader;
import data.serializing.DataRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.awt.image.ImageWatched;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static bot.role.RoleBotReady.checkGuild;

public class RoleBotListener extends ListenerAdapter {
    private Logger logger = LoggerFactory.getLogger(RoleBotListener.class);
    private Data data;
    private ResultsData resultsData;
    private ConfigLoader config;
    private Guild guild;
    private Category guildsCategory;
    private TextChannel warChannel;

    private List<String> roleBotCommandNames;

    public RoleBotListener(ConfigLoader config){
        DataRepository<Strings> strings = new DataRepository<>("arena\\strings");
        if(!strings.exists("strings")){
            strings.saveSerialized(new Strings());
        }
        this.config = config;
        data = new Data();
        resultsData = new ResultsData();
        roleBotCommandNames = new LinkedList<>();
        for(Method m : getClass().getDeclaredMethods()){
            if (m.isAnnotationPresent(SlashCommand.class))
                roleBotCommandNames.add(m.getAnnotation(SlashCommand.class).CommandName());
        }
    }

    @Override
    public void onReady(ReadyEvent event){
        guild = event.getJDA().getGuildById(data.getGameConfig().loadSerialized().getGuildId());
        checkGuild(guild, data);
        warChannel = guild.getTextChannelById(data.getGameConfig().loadSerialized().getChannelIds().get("war"));
        guildsCategory = guild.getCategoryById(data.getGameConfig().loadSerialized().getGuildsCategoryId());
        List<Member> members = guild.getMembers();
        for(Member m : members){
            // Check if any members don't have player data
            if(!m.getUser().isBot() && !data.getPlayers().exists(m.getIdLong())){
                createNewPlayer(m);
            }
        }
        Role kingRole = getKingRole();
        // assign king if there is no king
        if(guild.getMembersWithRoles(kingRole).size() == 0){
            Random random = new Random();
            Member newKing = members.get(random.nextInt(members.size()));
            assignNewKing(newKing);
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member newMember = event.getMember();
        createNewPlayer(newMember);
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Member leavingMember = event.getMember();
        data.getPlayers().delete(getAsPlayer(leavingMember)); // delete player
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.getAuthor().isBot()) {
            String message = event.getMessage().getContentRaw();
            if (message.charAt(0) == '!' && event.getAuthor().getIdLong() == 232675572772372481l) { // valid command and is Ben
                message = message.substring(1);
                switch (message.split(" ")[0]) {
                    case "sa":
                    case "spawn-activity":
                        for (int i = 0; i < Integer.parseInt(message.split(" ")[1]); i++) {
                            spawnActivity();
                        }
                        event.getChannel().sendMessage("Activity spawned").queue();
                        break;
                    case "sd":
                    case "spawn-dungeon":
                        for (int i = 0; i < Integer.parseInt(message.split(" ")[1]); i++) {
                        spawnDungeon();
                    }
                    event.getChannel().sendMessage("Dungeon spawned").queue();
                        break;
                }
            }
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if(!event.getUser().isBot() && event.isFromGuild()) {
            try {
                String name = event.getChannel().getName();
                for (Method m : getClass().getDeclaredMethods()) {
                    if (m.isAnnotationPresent(EmoteCommand.class)) {
                        EmoteCommand ec = m.getAnnotation(EmoteCommand.class);
                        if(ec.channelFrom().equals("none")){
                            if (event.getChannel().asTextChannel().getParentCategory().getName().contains(ec.categoryFrom())) {
                                m.invoke(this, event);
                            }
                        } else {
                            if (ec.channelFrom().equals(name) && event.getChannel().asTextChannel().getParentCategory().getName().contains(ec.categoryFrom())) {
                                m.invoke(this, event);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.getReaction().removeReaction().queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if(roleBotCommandNames.contains(event.getName())) {
                String name = event.getName();
                for (Method m : getClass().getDeclaredMethods()) {
                    if (m.isAnnotationPresent(SlashCommand.class)) {
                        SlashCommand sc = m.getAnnotation(SlashCommand.class);
                        if (sc.CommandName().equals(name)) {
                            if(!sc.subCommandName().equals("")){
                                String sName = event.getSubcommandName();
                                if(sc.subCommandName().equals(sName)){
                                    if(processSlashCommandAnnotations(sc, event)) {
                                        m.invoke(this, event);
                                    }
                                }
                            } else {
                                if(processSlashCommandAnnotations(sc, event)) {
                                    m.invoke(this, event);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.reply("Unable to complete the command. If this continues to happen submit a bug report with the /bug-report command").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        try {
            String name = event.getButton().getId();
            for (Method m : getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(ButtonCommand.class)) {
                    ButtonCommand bc = m.getAnnotation(ButtonCommand.class);
                    if (bc.CommandName().equals(name)) {
                        if(processButtonCommandAnnotations(bc, event)) {
                            m.invoke(this, event);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.reply("Unable to complete the command. If this continues to happen submit a bug report with the /bug-report command").setEphemeral(true).queue();
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Player p = getAsPlayer(event.getMember());
        List<Role> casteRole = getCasteRoles();
        for(Role role : event.getRoles()){
            if(casteRole.contains(role)){
                p.setCasteLevel(role.getName());
            }
        }
        data.saveData(p);
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        String newName = event.getMember().getEffectiveName();
        Player p = getAsPlayer(event.getMember());
        p.setName(newName);
        data.saveData(p);
    }

    /**
     * This is the method that does new day things
     * this automatically gets called at noon and midnight
     */
    public void newDay(){
        // tax
        KingData kd = data.getKingData().loadSerialized();
        if(kd.getTaxRoleID() != 0){
            Player king = getKingPlayer();
            int goldAmount = kd.getTaxAmount();
            for(Member m : guild.getMembersWithRoles(guild.getRoleById(kd.getTaxRoleID()))){
                Player p = getAsPlayer(m);
                king.increaseRawGold(p.payGold(goldAmount));
                data.saveData(p);
            }
            kd.resetTax();
        }
        kd.addDayToRun();
        kd.resetList();
        data.saveData(kd);

        // players
        for(Player p : data.getPlayers().getData()){
            p.newDay();
            data.saveData(p);
        }
        General g = data.getGeneral().loadSerialized();
        data.saveData(g.increaseDayCount());
        warChannel.sendMessageEmbeds(EmbedMessageGenerator.generateNewDay(g.getDayCount()));
    }

    /**
     * This task always gets run every minute by Bot.java
     * @see Bot
     */
    public void minuteTasks() throws NoSuchMethodException {
        Random random = new Random();
        GameConfigValues config = data.getGameConfig().loadSerialized();

        TextChannel encountersTC = guild.getTextChannelById(config.getChannelIds().get("encounters"));
        TextChannel tournamentsTC = guild.getTextChannelById(config.getChannelIds().get("tournaments"));
        TextChannel itemsTC = guild.getTextChannelById(config.getChannelIds().get("items"));
        TextChannel activitiesTC = guild.getTextChannelById(config.getChannelIds().get("activities"));
        TextChannel dungeonsTC = guild.getTextChannelById(config.getChannelIds().get("dungeons"));

        Emoji activate = guild.getEmojiById(config.getIconIds().get("Activate"));

        logger.info("Checking for spawns");
        if(random.nextDouble() <= config.getEncounterSpawnChance()){
            logger.info("\tSpawned encounter");
            Encounter encounter = Encounter.generate();
            encountersTC.sendMessageEmbeds(EmbedMessageGenerator.generate(encounter)).queue(message -> {
                encounter.setId(message.getId());
                message.addReaction(activate).queue();
                data.saveData(encounter);
            });
        }
        if(random.nextDouble() <= config.getShopItemSpawnChance()) {
            logger.info("\tSpawned shop item");
            ShopItem item = ShopItem.random();
            itemsTC.sendMessageEmbeds(EmbedMessageGenerator.generate(item)).queue(message -> {
                item.setId(message.getId());
                message.addReaction(activate).queue();
                data.saveData(item);
            });
        }
        if(random.nextDouble() <= config.getActivitySpawnChance()){
            spawnActivity();
        }
        if(random.nextDouble() <= config.getDungeonSpawnChance()) {
            spawnDungeon();
        }
        if(random.nextDouble() <= config.getTournamentSpawnChance()) {
            spawnTournament();
            logger.info("\tSpawned tournamnet");
        }

        Date now = new Date();

        // delete stuff
        for(Encounter encounter : data.getEncounters()){
            if(encounter.getDeparts().before(now)){
                data.deleteData(encounter);
                encountersTC.deleteMessageById(encounter.getId()).queue();
                logger.info("\tDeleting encounter: " + encounter.getId());
            }
        }

        for(Tournament tournament : data.getTournaments()){
            if(tournament.getDeparts().before(now)){
                // TODO run tournament
                data.deleteData(tournament);
                tournamentsTC.deleteMessageById(tournament.getId()).queue();
                logger.info("\tDeleting tournament: " + tournament.getId());
            }
        }

        for(ShopItem item : data.getShopItems()){
            if(item.getDeparts().before(now)){
                data.deleteData(item);
                itemsTC.deleteMessageById(item.getId()).queue();
                logger.info("\tDeleting shop item: " + item.getId());
            }
        }

        for(Activity activity : data.getActivities()){
            if(activity.getDeparts().before(now)){
                data.deleteData(activity);
                activitiesTC.deleteMessageById(activity.getId()).queue();
                logger.info("\tDeleting activity: " + activity.getId());
            }
        }

        for(Dungeon dungeon : data.getDungeons()){
            if(dungeon.getDeparts().before(now)){
                data.deleteData(dungeon);
                dungeonsTC.deleteMessageById(dungeon.getId()).queue();
                logger.info("\tDeleting dungeon: " + dungeon.getId());
            }
        }
    }

    private void spawnDungeon() {
        GameConfigValues config = data.getGameConfig().loadSerialized();
        TextChannel dungeonsTC = guild.getTextChannelById(config.getChannelIds().get("dungeons"));
        Emoji activate = guild.getEmojiById(config.getIconIds().get("Activate"));
        Dungeon dungeon = DungeonGenerator.GenerateRandomDungeon();
        DungeonGenerator.saveDungeon(dungeon);
        File dungeonPhoto = new File("arena\\dungeon photos\\dungeon.png");
        logger.info("\tSpawned dungeon");
        Message message = dungeonsTC.sendFiles(FileUpload.fromData(dungeonPhoto)).setEmbeds(EmbedMessageGenerator.generate(dungeon)).complete();
        dungeon.setId(message.getId());
        message.addReaction(activate).queue();
        data.saveData(dungeon);
        dungeonPhoto.renameTo(new File("arena\\dungeon photos\\" + dungeon.getId() + ".png"));
    }

    private void spawnActivity() {
        GameConfigValues config = data.getGameConfig().loadSerialized();
        TextChannel activitiesTC = guild.getTextChannelById(config.getChannelIds().get("activities"));
        Emoji activate = guild.getEmojiById(config.getIconIds().get("Activate"));
        logger.info("\tSpawned activity");
        Activity activity = Activity.random();
        activitiesTC.sendMessageEmbeds(EmbedMessageGenerator.generate(activity)).queue(message -> {
            activity.setId(message.getId());
            message.addReaction(activate).queue();
            data.saveData(activity);
        });
    }

    private void spawnTournament() {
        TextChannel tournamentsTC = guild.getTextChannelById(data.getGameConfig().loadSerialized().getChannelIds().get("tournaments"));
        Emoji activate = guild.getEmojiById(data.getGameConfig().loadSerialized().getIconIds().get("Activate"));
        logger.info("\tSpawned tournament");
        Tournament tournament = Tournament.random();
        tournamentsTC.sendMessageEmbeds(EmbedMessageGenerator.generate(tournament))
                .setActionRow(Button.secondary("fight_in_tournament", "Enter Tournament").withEmoji(activate)).queue(message -> {
            tournament.setId(message.getId());
            data.saveData(tournament);
        });
    }

    /**
     * @return King player
     */
    private Player getKingPlayer(){
        return getAsPlayer(guild.getMembersWithRoles(getKingRole()).get(0));
    }

    /**
     * Creates a new player with a random caste role.
     * Also saves the player.
     * @param member new player
     */
    private void createNewPlayer(Member member){
        Player newPlayer = new Player(member.getIdLong(), member.getEffectiveName(), data.getGameConfig().loadSerialized());
        guild.modifyMemberRoles(member, null, getCasteRoles()).queue();
        newPlayer.setCasteLevel(assignRandomCasteRole(member));
        data.getPlayers().saveSerialized(newPlayer); // save new player
    }

    /**
     * Automatically sends a message to the default war channel about whom the new king is.
     * Removes any other caste roles, and assigns the king role
     * @param king Player to become the new king
     */
    private void assignNewKing(Member king){
        warChannel.sendMessageEmbeds(EmbedMessageGenerator.generateNewKing(king)).queue();
        // remove old caste role if any
        // add king role
        List<Role> remove = getCasteRoles();
        System.out.println(remove.size());
        guild.modifyMemberRoles(king, new LinkedList<Role>(Arrays.asList(new Role[]{getKingRole()})), remove).queue();
    }

    /**
     * Swaps the caste roles between two player of the server
     * @param player1
     * @param player2
     */
    private void swapCasteRoles(Player player1, Player player2){
        Member am = guild.getMemberById(player1.getId());
        Member ad = guild.getMemberById(player2.getId());
        swapCasteRoles(am, ad);
    }


    /**
     * Swaps the caste roles between two members of the server
     * @param member1 Member 1 to swap roles with
     * @param member2 Member 2 to swap roles with
     */
    private void swapCasteRoles(Member member1, Member member2){
        Role attackerRole = getCasteRoleOfPlayer(member1);
        Role defenderRole = getCasteRoleOfPlayer(member2);
        List<Role> attackerRoles = new LinkedList<>(Arrays.asList(new Role[]{attackerRole}));
        List<Role> defenderRoles = new LinkedList<>(Arrays.asList(new Role[]{defenderRole}));
        guild.modifyMemberRoles(member1, defenderRoles, attackerRoles).queue();
        guild.modifyMemberRoles(member2, attackerRoles, defenderRoles).queue();
    }

    /**
     * Assigns a random caste role that isn't the king to a player
     * @param member Member to assign the role too
     */
    private String assignRandomCasteRole(Member member){
        Random random = new Random();
        List<Long> roleIds = new LinkedList<>(data.getGameConfig().loadSerialized().getRoleIds().values());
        Role randomRole = guild.getRoleById(roleIds.get(random.nextInt(roleIds.size())));
        guild.addRoleToMember(member, randomRole).queue();
        return randomRole.getName();
    }

    /**
     * Gets a list of caste roles in role form
     * @return a list of caste roles in role form
     */
    private List<Role> getCasteRolesWithKing(){
        List<Role> roles = new LinkedList<>();
        for(Role role : guild.getRoles()){
            if(data.getGameConfig().loadSerialized().getRoleIds().values().contains(role.getIdLong())){
                roles.add(role);
            }
        }
        Comparator<Role> roleComparator = Comparator.comparing(Role::getPosition);
        Collections.sort(roles, roleComparator);
        return roles;
    }

    /**
     * Gets a list of caste roles in role form
     * @return a list of caste roles in role form
     */
    private List<Role> getCasteRoles(){
        List<Role> roles = new LinkedList<>();
        List<Long> ids = new LinkedList<>(data.getGameConfig().loadSerialized().getRoleIds().values());
        ids.remove(data.getGameConfig().loadSerialized().getRoleIds().get("King"));
        for(Role role : guild.getRoles()){
            if(ids.contains(role.getIdLong())){
                roles.add(role);
            }
        }
        Comparator<Role> roleComparator = Comparator.comparing(Role::getPosition);
        Collections.sort(roles, roleComparator);
        return roles;
    }

    private int getCasteRoleLevel(Member member){
        Role role = getCasteRoleOfPlayer(member);
        List<Long> ids = new LinkedList<>(data.getGameConfig().loadSerialized().getRoleIds().values());
        if(role.getIdLong() == data.getGameConfig().loadSerialized().getRoleIds().get("King")){
            return ids.size() + 1;
        } else {
            return ids.size() - ids.indexOf(role.getIdLong());
        }
    }

    private int getCasteRoleLevel(Player player){
        return getCasteRoleLevel(getAsMember(player));
    }

    /**
     * @param player Player to be checking for the caste roles for
     * @return The caste role they are assigned
     */
    private Role getCasteRoleOfPlayer(Player player){
        return getCasteRoleOfPlayer(guild.getMemberById(player.getId()));
    }

    /**
     * @param member Member to be checking for the caste roles for
     * @return The role they are assigned
     */
    private Role getCasteRoleOfPlayer(Member member){
        List<Role> roles = getCasteRoles();
        for(Role role : member.getRoles()){
            if(roles.contains(role)){
                return role;
            }
        }
        return null;
    }

    /**
     * @param player Player to be checking for the caste roles for
     * @return The name of the role
     */
    private String getCasteRoleNameOfPlayer(Player player){
        return getCasteRoleNameOfPlayer(guild.getMemberById(player.getId()));
    }

    /**
     * @param member Member to be checking for the caste roles for
     * @return The name of the role
     */
    private String getCasteRoleNameOfPlayer(Member member){
        Role role = getCasteRoleOfPlayer(member);
        if(role != null){
            return role.getName();
        } else {
            return "";
        }
    }

    private Member getAsMember(Player player) {
        return guild.getMemberById(player.getId());
    }

    private Player getAsPlayer(Member member){
        return data.getPlayers().loadSerialized(member.getIdLong());
    }

    private boolean isKing(Player player) {
        return isKing(getAsMember(player));
    }

    private boolean isKing(Member member) {
        return member.getRoles().contains(getKingRole());
    }

    /**
     * @return The king caste role as a role object
     */
    private Role getKingRole(){
        return guild.getRoleById(data.getGameConfig().loadSerialized().getRoleIds().get("King"));
    }

    /**
     * @return war text channel object
     */
    private TextChannel getWarTextChannel(){
        return guild.getTextChannelById(data.getGameConfig().loadSerialized().getChannelIds().get("war"));
    }

    public void hourBeforeNewDay() {
        for(Player p : data.getPlayers().getData()){
            if(p.isRemind()){
                Member m = getAsMember(p);
                m.getUser().openPrivateChannel().queue(channel -> {
                    channel.sendMessageEmbeds(EmbedMessageGenerator.generateRemindMessage()).queue();
                });
            }
        }
    }

    public bot.role.data.structures.Guild getPlayerGuild(Player p){
        for(bot.role.data.structures.Guild guild : data.getGuilds()){
            if(guild.isInGuild(p.getIdLong())){
                return guild;
            }
        }
        return null;
    }

    public bot.role.data.structures.Guild getGuildByTextId(long id){
        for(bot.role.data.structures.Guild guild : data.getGuilds()){
            if(guild.getIds().getTextChannel() == id){
                return guild;
            }
        }
        return null;
    }

    private boolean isGuildLeader(Member member) {
        boolean found = false;
        for(bot.role.data.structures.Guild guild : data.getGuilds()){
            if(guild.isGuildOwner(member.getIdLong())){
                found = true;
                break;
            }
        }
        return found;
    }

    private boolean isGuildOfficerPermissions(Member member) {
        for(bot.role.data.structures.Guild guild : data.getGuilds()){
            if(guild.isInGuild(member.getIdLong())){
                for(Role role : member.getRoles()) {
                    if (guild.isGuildOwner(role.getIdLong()) || guild.isGuildOfficer(role.getIdLong())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public bot.role.data.structures.Guild getMemberGuild(Member m){
        return getPlayerGuild(getAsPlayer(m));
    }

    private bot.role.data.structures.Guild getGuildByName(String guildName) {
        if(data.getGuilds().exists(guildName)){
            return data.getGuilds().loadSerialized(guildName);
        }
        return null;
    }

    private boolean processButtonCommandAnnotations(ButtonCommand bc, ButtonInteractionEvent event) {
        if(bc.isOwner()){
            if (!isGuildLeader(event.getMember())) {
                event.reply("Only the guild leader is allowed to do this action").setEphemeral(true).queue();
                return false;
            }
        }
        if(bc.isOfficerPermission()){
            if(!isGuildOfficerPermissions(event.getMember())){
                event.reply("You must have officer permissions or above to do this action").setEphemeral(true).queue();
                return false;
            }
        }

        return true;
    }

    /**
     * Process slash command annotation options
     * @param sc Slash command annotation
     * @param event slash command event
     * @return false if the processing fails, true if it succeeds
     */
    private boolean processSlashCommandAnnotations(SlashCommand sc, SlashCommandInteractionEvent event){
        if(sc.KingOnly()){
            if(!isKing(event.getMember())) { // is king
                event.reply("Only the king can use this command").setEphemeral(true).queue();
                return false;
            }
        }
        if(sc.positiveGold()){
            int gold = event.getOption("gold").getAsInt();
            if(gold <= 0){
                event.reply("Gold must be a positive amount").setEphemeral(true).queue();
                return false;
            }
        }
        if(sc.warChannelOnly()){
            if(event.getChannel().getIdLong() != warChannel.getIdLong()){ // is in war
                event.reply("You can only do this in <#" + warChannel.getIdLong() + ">").setEphemeral(true).queue();
                return false;
            }
        }
        if(sc.validCasteRole()){
            if(!getCasteRoles().contains(event.getOption("role").getAsRole())){
                event.reply("The role you have selected is not a caste role").setEphemeral(true).queue();
                return false;
            }
        }
        if(sc.isInGuild()){
            boolean found = false;
            for(bot.role.data.structures.Guild guild : data.getGuilds()){
                if(guild.isInGuild(event.getMember().getIdLong())){
                    found = true;
                    break;
                }
            }
            if(!found){
                event.reply("You must be in a guild to use this command").setEphemeral(true).queue();
                return false;
            }
        }
        if(sc.isNotInGuild()){
            for(bot.role.data.structures.Guild guild : data.getGuilds()){
                if(guild.isInGuild(event.getMember().getIdLong())){
                    event.reply("You must not be in a guild to use this command").setEphemeral(true).queue();
                    return false;
                }
            }
        }
        if(sc.isLeaderOfGuild()){
            if(!isGuildLeader(event.getMember())){
                event.reply("You must be a guild owner to use this command").setEphemeral(true).queue();
                return false;
            }
        }
        Player player = getAsPlayer(event.getMember());
        if(player.activitiesLeftToday() < sc.activityCheck()){
            event.reply("You do not have enough activities left").setEphemeral(true).queue();
            return false;
        }
        if(sc.isFromGuildChat()){
            long channelId = event.getChannel().getIdLong();
            boolean found = false;
            for(bot.role.data.structures.Guild guild : data.getGuilds()){
                if(guild.getIds().getTextChannel() == channelId){
                    found = true;
                    break;
                }
            }
            if(!found){
                event.reply("This has to be done in a guild chat").setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    @SlashCommand(CommandName = "leaderboard")
    private void leaderboardSlashCommand(SlashCommandInteractionEvent event){
        boolean includeItems = event.getOption("include-items") != null ? event.getOption("include-items").getAsBoolean() : true;
        Function<Player, Integer> function;
        switch (event.getSubcommandName()){
            case "activities":
                function = Player::activitiesLeftToday;
                break;
            case "total":
                function = includeItems ? Player::getTotalStatsWithItems : Player::getTotalStats;
                break;
            case "strength":
                function = includeItems ? Player::getStrengthStat : Player::getRawStrengthStat;
                break;
            case "knowledge":
                function = includeItems ? Player::getKnowledgeStat : Player::getRawKnowledgeStat;
                break;
            case "magic":
                function = includeItems ? Player::getMagicStat : Player::getRawKnowledgeStat;
                break;
            case "agility":
                function = includeItems ? Player::getAgilityStat : Player::getRawAgilityStat;
                break;
            case "stamina":
                function = includeItems ? Player::getStaminaStat : Player::getRawStaminaStat;
                break;
            case "gold":
                function = Player::getGold;
                break;
            case "wins":
                function = Player::getWins;
                break;
            case "losses":
                function = Player::getLosses;
                break;
            default:
                event.reply("Invalid stat. Valid stats are Strength, Knowledge, Magic, Agility, Stamina, Gold, Wins, Losses and Total").setEphemeral(true).queue();
                return;
        }

        event.replyEmbeds(EmbedMessageGenerator.generateLeaderboard(function, event.getSubcommandName(), data)).queue();
    }

    @SlashCommand(CommandName = "role-stats", validCasteRole = true)
    private void roleStatsSlashCommand(SlashCommandInteractionEvent event){
        Role casteRole = event.getOption("role").getAsRole();
        List<Player> players = new LinkedList<>();
        for(Member m : guild.getMembersWithRoles(casteRole)){
            players.add(getAsPlayer(m));
        }
        event.replyEmbeds(EmbedMessageGenerator.generateRoleStat(players, casteRole.getName(),
                casteRole.getIcon() == null ? "" : casteRole.getIcon().getIconUrl())).queue();
    }

    @SlashCommand(CommandName = "stats")
    private void statsSlashCommand(SlashCommandInteractionEvent event){
        Member member = event.getOption("player") != null ? member = event.getOption("player").getAsMember() : event.getMember();
        if(member.getUser().isBot()){
            event.reply("Bots don't have stats...yet").setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(EmbedMessageGenerator.generateStatsMessage(getAsPlayer(member), member)).queue();
    }

    @SlashCommand(CommandName = "pass-law", KingOnly = true, warChannelOnly = true)
    private void passLawSlashCommand(SlashCommandInteractionEvent event){
        KingData kd = data.getKingData().loadSerialized();
        if(kd.getKingDayRun() < 8){
            event.reply("You have to be king for 8 or more days to pass a law").setEphemeral(true).queue();
            return;
        }
        String law = event.getOption("law").getAsString();
        event.reply("Your law has been submitted to the church of shlongbot. It shall be reviewed. Shlongbot bless.").queue();
        guild.getMemberById(232675572772372481l).getUser().openPrivateChannel().queue(channel -> {
            channel.sendMessage("A new law was proposed by " + event.getMember().getEffectiveName() + "\n" + law).queue();
        });
        kd.resetRun();
        data.saveData(kd);
    }

    @SlashCommand(CommandName = "honorable-promotion", KingOnly = true, warChannelOnly = true, activityCheck = 1)
    private void honorablePromotionSlashCommand(SlashCommandInteractionEvent event){
        Player king = getAsPlayer(event.getMember());
        Member member1 = event.getOption("citizen-one").getAsMember();
        Member member2 = event.getOption("citizen-two").getAsMember();
        if(member1.getUser().isBot() || member2.getUser().isBot()){ // making sure members arent bots
            event.reply("Both citizens cannot be bots").setEphemeral(true).queue();
            return;
        }
        Role role1 = getCasteRoleOfPlayer(member1);
        Role role2 = getCasteRoleOfPlayer(member2);
        if(role1 == null || role2 == null){ // both of them are in a caste
            event.reply("Both citizens must be in a caste!").setEphemeral(true).queue();
            return;
        }
        swapCasteRoles(member1, member2);
        event.replyEmbeds(EmbedMessageGenerator.generateRollSwap(getAsPlayer(member1), getAsPlayer(member2))).queue();
        king.activityCompleted();
        data.saveData(king);
    }

    @SlashCommand(CommandName = "propose-tax", KingOnly = true, activityCheck = 1, warChannelOnly = true, positiveGold = true, validCasteRole = true)
    private void proposeTaxSlashCommand(SlashCommandInteractionEvent event){
        Player king = getAsPlayer(event.getMember());
        Role role = event.getOption("role").getAsRole();
        int gold = event.getOption("gold").getAsInt();
        int max = data.getGameConfig().loadSerialized().getGoldTaxMax();
        if(gold > max){ // if they tax more then they are allowed too
            event.reply("You can only tax less than " + max + " gold!").setEphemeral(true).queue();
            return;
        }
        KingData kingData = data.getKingData().loadSerialized();
        kingData.setTax(role.getIdLong(), gold);
        event.replyEmbeds(EmbedMessageGenerator.generateProposeTaxMessage(king, role, gold)).queue();
    }

    @SlashCommand(CommandName = "distribute-wealth", KingOnly = true, positiveGold = true, warChannelOnly = true, validCasteRole = true)
    private void distributeWealthSlashCommand(SlashCommandInteractionEvent event){
        Role role = event.getOption("role").getAsRole();
        int gold = event.getOption("gold").getAsInt();
        int total = gold;
        Player king = getAsPlayer(event.getMember());
        List<Player> players = new LinkedList<>();
        for(Member m : guild.getMembersWithRoles(role)){
            if(!m.getUser().isBot()) {
                players.add(getAsPlayer(m));
            }
        }
        Random random = new Random();
        while(gold > 0){
            players.get(random.nextInt(players.size())).increaseRawGold(1);
            king.increaseRawGold(-1);
            gold--;
        }
        players.add(king);
        data.getPlayers().saveSerialized(players);
        event.replyEmbeds(EmbedMessageGenerator.generateDistributeWealth(king, total, role)).queue();
    }

    @SlashCommand(CommandName = "fight-stats")
    private void fightStatsSlashCommand(SlashCommandInteractionEvent event){
        String fightId = event.getOption("id").getAsString();
        if(resultsData.getChallenges().exists(fightId)) {
            ChallengeFightResults cfr = resultsData.getChallenges().loadSerialized(fightId);
            event.replyEmbeds(EmbedMessageGenerator.generate(cfr, EmbedMessageGenerator.Detail.COMPLEX)).queue();
        } else {
            event.reply("No fight exists with that id.").queue();
        }
    }

    @SlashCommand(CommandName = "pay-citizen", positiveGold = true, warChannelOnly = true)
    private void payCitizenSlashCommand(SlashCommandInteractionEvent event) {
        Member takerMember = event.getOption("citizen").getAsMember();
        Member giverMember = event.getMember();
        if(takerMember.getUser().isBot()){
           event.reply("You cannot give a bot gold.").setEphemeral(true).queue();
           return;
        }
        int gold = event.getOption("gold").getAsInt();
        Player giverPlayer = getAsPlayer(giverMember);
        if(giverPlayer.getGold() < gold){
            // player does not have enough gold to give
            event.reply("You do not have " + gold + " gold to give!").setEphemeral(true).queue();
        }
        Player takerPlayer = getAsPlayer(takerMember);
        takerPlayer.increaseRawGold(gold);
        giverPlayer.increaseRawGold(-gold);
        event.replyEmbeds(EmbedMessageGenerator.generatePayCitizen(giverPlayer, takerPlayer, gold)).queue();
        data.getPlayers().saveSerialized(giverPlayer, takerPlayer);
    }

    @SlashCommand(CommandName = "remind")
    private void remindSlashCommand(SlashCommandInteractionEvent event){
        boolean remind = event.getOption("should-remind") == null ? true : event.getOption("should-remind").getAsBoolean();
        Player player = getAsPlayer(event.getMember());
        player.setRemind(remind);
        if(remind){
            event.reply("You have opted into recieving reminders.").setEphemeral(true).queue();
        } else {
            event.reply("You have opted out of recieving reminders.").setEphemeral(true).queue();
        }
        data.saveData(player);
    }

    @SlashCommand(CommandName = "manage-inventory")
    private void manageInventorySlashCommand(SlashCommandInteractionEvent event){
        int slotOne = event.getOption("slot-one").getAsInt();
        int slotTwo = event.getOption("slot-two").getAsInt();
        if(slotOne > 5 || slotOne < 1 || slotTwo > 5 || slotTwo < 1){
            event.reply("Both slots must be between 1 and 5").setEphemeral(true).queue();
            return;
        }
        Player player = getAsPlayer(event.getMember());
        player.swapSlots(slotOne, slotTwo);
        data.saveData(player);
        event.reply("Inventory slots " + slotOne + " and " + slotTwo + " have been swapped.").queue();
    }

    @ButtonCommand(CommandName = "cursor_up", isOfficerPermission = true)
    private void guildReorderMoveCursorUp(ButtonInteractionEvent event){
        int selectedIndex = getSelectedIndex(event.getMessage().getEmbeds().get(0).getDescription());
        LinkedList<String> names = clearSelected(event.getMessage().getEmbeds().get(0).getDescription().split("\n"));
        if(selectedIndex - 1 < 0){
            selectedIndex = names.size() - 1;
        } else {
            selectedIndex--;
        }
        event.editMessageEmbeds(EmbedMessageGenerator.generateGuildOrder(names, selectedIndex)).queue();
    }

    @ButtonCommand(CommandName = "cursor_down", isOfficerPermission = true)
    private void guildReorderMoveCursorDown(ButtonInteractionEvent event){
        int selectedIndex = getSelectedIndex(event.getMessage().getEmbeds().get(0).getDescription());
        LinkedList<String> names = clearSelected(event.getMessage().getEmbeds().get(0).getDescription().split("\n"));
        if(selectedIndex + 1 >= names.size()){
            selectedIndex = 0;
        } else {
            selectedIndex++;
        }
        event.editMessageEmbeds(EmbedMessageGenerator.generateGuildOrder(names, selectedIndex)).queue();
    }

    @ButtonCommand(CommandName = "move_selected_up", isOfficerPermission = true)
    private void guildReorderMoveSelectedUp(ButtonInteractionEvent event){
        int selectedIndex = getSelectedIndex(event.getMessage().getEmbeds().get(0).getDescription());
        LinkedList<String> names = clearSelected(event.getMessage().getEmbeds().get(0).getDescription().split("\n"));

    }

    @ButtonCommand(CommandName = "move_selected_up", isOfficerPermission = true)
    private void guildReorderMoveSelectedDown(ButtonInteractionEvent event){
        int selectedIndex = getSelectedIndex(event.getMessage().getEmbeds().get(0).getDescription());
    }

    private LinkedList<String> clearSelected(String[] names){
        LinkedList<String> newNames = new LinkedList<>();
        for(String name : names){
            newNames.add(name.replace("**>", "").replace("<**", ""));
        }
        return newNames;
    }

    private int getSelectedIndex(String desc){
        String[] players = desc.split("\n");
        for(int i = 0; i < players.length; i++){
            if(players[i].contains("**>") && players[i].contains("<**")){
                return i;
            }
        }
        return 0;
    }

    @SlashCommand(CommandName = "guild", subCommandName = "reorder", isFromGuildChat = true)
    private void guildReorderSlashCommand(SlashCommandInteractionEvent event){
        bot.role.data.structures.Guild guild = getGuildByTextId(event.getChannel().getIdLong());
        LinkedList<String> names = new LinkedList<>();
        for(Long id : guild.getMembers()){
            names.add(this.guild.getMemberById(id).getEffectiveName());
        }
        event.replyEmbeds(EmbedMessageGenerator.generateGuildOrder(names, 0))
                .setActionRow(
                        Button.primary("move_selected_up", "Move up"),
                        Button.primary("move_selected_down", "Move down"),
                        Button.secondary("cursor_up", "Cursor up"),
                        Button.secondary("cursor_down", "Cursor down"),
                        Button.success("confirm_order", "Confirm order")
                ).queue();
    }

    @SlashCommand(CommandName = "guild", subCommandName = "join", isNotInGuild = true)
    private void guildJoinSlashCommand(SlashCommandInteractionEvent event){
        String guildName = event.getOption("guild-name").getAsString();
        bot.role.data.structures.Guild guild = getGuildByName(guildName);
        if(guild == null){
            event.reply("This guild does not exists").setEphemeral(true).queue();
            return;
        }
        if(guild.isPublicGuild()){
            guild.addToGuild(event.getMember().getIdLong());
            data.saveData(guild);
            Role member = this.guild.getRoleById(guild.getIds().getMemberRole());
            this.guild.addRoleToMember(event.getMember(), member).queue();
            event.reply("You have joined the guild!").setEphemeral(true).queue();
        } else {
            TextChannel guildTC = this.guild.getTextChannelById(guild.getIds().getTextChannel());
            guildTC.sendMessageEmbeds(EmbedMessageGenerator.generateGuildJoinRequest(event.getMember()))
                    .setActionRow(
                            Button.primary("accept_guild_join", "Accept"),
                            Button.secondary("reject_guild_join", "Reject")
                    ).queue();
            event.reply("A request to join the guild has been sent").setEphemeral(true).queue();
        }

    }

    @ButtonCommand(CommandName = "accept_guild_join", isOfficerPermission = true)
    private void acceptGuildJoin(ButtonInteractionEvent event){
        String memberId = event.getMessage().getEmbeds().get(0).getFooter().getText();
        Member member = guild.getMemberById(memberId);
        // disables buttons
        event.getMessage().editMessageComponents().queue();
        if(member == null){
            event.reply("This member no longer exists").queue();
            return;
        }
        event.reply(member.getEffectiveName() + " has been accepted into the guild").queue();
        bot.role.data.structures.Guild playerGuild = getGuildByTextId(event.getMessageChannel().getIdLong());
        playerGuild.addToGuild(Long.parseLong(memberId));
        data.saveData(playerGuild);
        Role memberRole = guild.getRoleById(playerGuild.getIds().getMemberRole());
        guild.addRoleToMember(member, memberRole).queue();
        member.getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("You have been accepted into the " + playerGuild.getId() + " guild!").queue();
        });
    }

    @ButtonCommand(CommandName = "reject_guild_join", isOfficerPermission = true)
    private void rejectGuildJoin(ButtonInteractionEvent event){
        String memberId = event.getMessage().getEmbeds().get(0).getFooter().getText();
        Member member = guild.getMemberById(memberId);
        Message message = event.getMessage();
        // disables buttons
        event.getMessage().editMessageComponents().queue();
        if(member == null){
            event.reply("This member no longer exists").queue();
            return;
        }
        event.reply(member.getEffectiveName() + " has been rejected entrance into the guild").queue();
        bot.role.data.structures.Guild playerGuild = getGuildByTextId(event.getMessageChannel().getIdLong());
        member.getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("You have been rejected entrance into the " + playerGuild.getId() + " guild").queue();
        });
    }

    @SlashCommand(CommandName = "guild", subCommandName = "leave", isInGuild = true)
    private void guildLeaveSlashCommand(SlashCommandInteractionEvent event){
        bot.role.data.structures.Guild guild = getMemberGuild(event.getMember());
        guild.removeFromGuild(event.getMember().getIdLong());
        if(guild.isEmpty()){
            data.getGuilds().delete(guild);
            this.guild.getTextChannelById(guild.getIds().getTextChannel()).delete().queue();
            this.guild.getVoiceChannelById(guild.getIds().getVoiceChannel()).delete().queue();
            this.guild.getRoleById(guild.getIds().getOwnerRole()).delete().queue();
            this.guild.getRoleById(guild.getIds().getOfficerRole()).delete().queue();
            this.guild.getRoleById(guild.getIds().getMemberRole()).delete().queue();
        } else{
            Role owner = this.guild.getRoleById(guild.getIds().getOwnerRole());
            Role officer = this.guild.getRoleById(guild.getIds().getOfficerRole());
            Role member = this.guild.getRoleById(guild.getIds().getMemberRole());
            this.guild.removeRoleFromMember(event.getMember(), owner).queue();
            this.guild.removeRoleFromMember(event.getMember(), officer).queue();
            this.guild.removeRoleFromMember(event.getMember(), member).queue();
            data.saveData(guild);
        }
        event.reply("You have left the " + guild.getId() + " guild").setEphemeral(true).queue();
    }

    @SlashCommand(CommandName = "guild", subCommandName = "create", isNotInGuild = true)
    private void guildCreateSlashCommand(SlashCommandInteractionEvent event){
        String guildName = event.getOption("guild-name").getAsString();
        if(data.getGuilds().exists(guildName)){
            event.reply("A guild with that name already exists!").setEphemeral(true).queue();
            return;
        }
        TextChannel tc = guild.createTextChannel(guildName, guildsCategory).complete();
        VoiceChannel vc = guild.createVoiceChannel(guildName, guildsCategory).complete();
        Role owner = guild.createRole().setName(guildName + " owner").complete();
        guild.addRoleToMember(event.getMember(), owner).queue();
        Role officer = guild.createRole().setName(guildName + " officer").complete();
        Role member = guild.createRole().setName(guildName + " member").complete();
        tc.getManager().putRolePermissionOverride(
                owner.getIdLong(),
                new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL})),
                new LinkedList<> ()
        ).putRolePermissionOverride(
                officer.getIdLong(),
                new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL})),
                new LinkedList<> ()
        ).putRolePermissionOverride(
                member.getIdLong(),
                new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL})),
                new LinkedList<> ()
        ).putRolePermissionOverride(
                guild.getPublicRole().getIdLong(),
                new LinkedList<> (),
                new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL}))
        ).queue();
        vc.getManager().putRolePermissionOverride(
                owner.getIdLong(),
                new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL})),
                new LinkedList<> ()
        ).putRolePermissionOverride(
                officer.getIdLong(),
                new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL})),
                new LinkedList<> ()
        ).putRolePermissionOverride(
                member.getIdLong(),
                new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL})),
                new LinkedList<> ()
        ).putRolePermissionOverride(
                guild.getPublicRole().getIdLong(),
                new LinkedList<> (),
                new LinkedList<> (Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL}))
        ).queue();
        data.saveData(new bot.role.data.structures.Guild(guildName, event.getMember().getIdLong(), tc.getIdLong(), vc.getIdLong(), owner.getIdLong(), officer.getIdLong(), member.getIdLong(), event.getOption("public").getAsBoolean()));
        event.reply("Your guild has been created!").setEphemeral(true).queue();
    }

    @SlashCommand(CommandName = "challenge", warChannelOnly = true, activityCheck = 1)
    private void challengeSlashCommand(SlashCommandInteractionEvent event){
        /* Pre challenge check */
        Member attackerMember = event.getMember();
        Member defenderMember = event.getOption("player").getAsMember();
        if(defenderMember.getUser().isBot()){   // if the defender is a bot
            event.reply("You cannot challenge a bot...yet").setEphemeral(true).queue();
            return;
        }
        Player attacker = getAsPlayer(attackerMember);
        Player defender = getAsPlayer(defenderMember);
        if(isKing(defenderMember)){
            KingData kd = data.getKingData().loadSerialized();
            if(!kd.canFightKing(attacker.getIdLong())){
                event.reply("You have already fought the king today!").setEphemeral(true).queue();
                return;
            }
            kd.addPlayerKingFought(attacker.getIdLong());
            data.saveData(kd);
        } else {
            if(!defender.canDefend()){
                event.reply("That player has already defended too many challenges today.").setEphemeral(true).queue();
                return;
            }
        }

        /* end pre challenge check */

        int attackerCasteLevel = getCasteRoleLevel(attackerMember);
        int defenderCasteLevel = getCasteRoleLevel(defenderMember);
        boolean attackingUp = attackerCasteLevel < defenderCasteLevel;
        int defenderPaddingLevel = attackingUp ? defenderCasteLevel - attackerCasteLevel : 0;
        if(defenderMember.isBoosting()) defenderPaddingLevel++;
        if(isKing(defenderMember)) defenderPaddingLevel++;
        int paddingMultiplier = (int)(attacker.getStatBlockWithItems().total() * data.getGameConfig().loadSerialized().getPaddingMultiplier());
        int padding = paddingMultiplier * defenderPaddingLevel;
        StatBlock totals = StatBlock.add(attacker.getStatBlockWithItems(), defender.getStatBlockWithItems());
        totals.addToAll(padding);
        Random r = new Random();
        StatBlock rolled = new StatBlock(
                r.nextInt(totals.getMagic()),
                r.nextInt(totals.getKnowledge()),
                r.nextInt(totals.getStamina()),
                r.nextInt(totals.getStrength()),
                r.nextInt(totals.getAgility())
        );

        int attackerPoints = 0;

        for(String stat : rolled.getAllStats().keySet()){
            int valRolled = rolled.getAllStats().get(stat);
            if(valRolled <= attacker.getStatBlockWithItems().getAllStats().get(stat)){
                attackerPoints++;
            }
        }
        int goldAmount = 0;
        StatBlock resultChange = null;
        boolean bounty = false;
        int base = 0;
        if(attackerPoints >= 3){
            // attacker won
            bounty = defender.getWinStreak() > 4;
            base = bounty ? defender.getWinStreak() * 10 : 0;
            attacker.increaseWins();
            defender.increaseLosses();
            if(attackingUp){
                // attacking up
                goldAmount = r.nextInt(20) - 10 + 20;
                attacker.increaseGold(defender.payGold(goldAmount) + base);
                swapCasteRoles(attackerMember, defenderMember);
            } else {
                // attacking down
                goldAmount = r.nextInt(20) - 10 + 20;
                attacker.increaseGold(defender.payGold(goldAmount) + base);
            }
        } else {
            // attacker lost
            attacker.increaseLosses();
            defender.increaseWins();
            if(attackingUp){
                // attacking up
                String stat = StatBlock.getBiggestDifference(attacker.getStatBlockWithItems(), defender.getStatBlockWithItems());
                int statChangeAmount = r.nextInt(3) + 1;
                resultChange = StatBlock.generateByStat(stat, statChangeAmount);
                attacker.increaseByStatBlock(resultChange);
                goldAmount = r.nextInt(20) - 10 + 20;
                defender.increaseGold(attacker.payGold(goldAmount));
            } else {
                // attacking down
                goldAmount = r.nextInt(20) - 10 + 20;
                defender.increaseGold(attacker.payGold(goldAmount));
                swapCasteRoles(attackerMember, defenderMember);
            }
        }

        ChallengeFightResults cfr = new ChallengeFightResults(
                resultsData.getChallenges().generateID(),
                attacker,
                defender,
                attackerPoints,
                rolled,
                resultChange,
                getCasteRoleNameOfPlayer(attacker),
                getCasteRoleNameOfPlayer(defender),
                paddingMultiplier,
                defenderPaddingLevel,
                goldAmount,
                attackingUp,
                bounty,
                base
        );

        attacker.activityCompleted();
        defender.hasDefended();
        data.saveData(attacker, defender);
        resultsData.saveData(cfr);
        data.saveData(data.getGeneral().loadSerialized().increaseChallengesFought());
        event.replyEmbeds(EmbedMessageGenerator.generate(cfr, EmbedMessageGenerator.Detail.SIMPLE)).queue();
    }


    public void christmas() {
    }
}
