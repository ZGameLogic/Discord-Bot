package bot.role;

import bot.role.data.Data;
import bot.role.data.ResultsData;
import bot.role.data.results.ChallengeFightResults;
import bot.role.data.structures.General;
import bot.role.data.structures.KingData;
import bot.role.data.structures.Player;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.structures.StatBlock;
import bot.role.data.structures.annotations.SlashCommand;
import bot.role.helpers.EmbedMessageGenerator;
import data.ConfigLoader;
import data.serializing.DataCacher;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.lang.reflect.Method;
import java.util.*;

public class RoleBotListener extends ListenerAdapter {

    private Data data;
    private ResultsData resultsData;
    private ConfigLoader config;
    private Guild guild;
    private TextChannel warChannel;

    private List<String> roleBotCommandNames;

    public RoleBotListener(ConfigLoader config){
        DataCacher<Strings> strings = new DataCacher<Strings>("arena\\strings");
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
        warChannel = guild.getTextChannelById(data.getGameConfig().loadSerialized().getGeneralChannelId());
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
        data.getPlayers().delete(leavingMember.getId()); // delete player
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMember().getIdLong() == 232675572772372481l){
            if(event.getMessage().getContentRaw().equals("!new-day")) {
                newDay();
                event.getChannel().sendMessage("A new day has passed!").queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {}

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {}

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {}

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if(roleBotCommandNames.contains(event.getName())) {
                String name = event.getName();
                for (Method m : getClass().getDeclaredMethods()) {
                    if (m.isAnnotationPresent(SlashCommand.class)) {
                        SlashCommand sc = m.getAnnotation(SlashCommand.class);
                        if (sc.CommandName().equals(name)) {
                            if(processSlashCommandAnnotations(sc, event)) {
                                m.invoke(this, event);
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
        assignRandomCasteRole(member);
        data.getPlayers().saveSerialized(newPlayer); // save new player
    }

    /**
     * Automatically sends a message to the default war channel about whom the new king is.
     * Removes any other caste roles, and assigns the king role
     * @param king Player to become the new king
     */
    private void assignNewKing(Member king){
        // TODO send message

        // remove old caste role if any
        // add king role
        guild.modifyMemberRoles(king, new LinkedList<Role>(Arrays.asList(new Role[]{getKingRole()})), getCasteRoles()).queue();
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
    private void assignRandomCasteRole(Member member){
        Random random = new Random();
        List<Long> roleIds = data.getGameConfig().loadSerialized().getRoleIds();
        Role randomRole = guild.getRoleById(roleIds.get(random.nextInt(roleIds.size())));
        guild.addRoleToMember(member, randomRole).queue();
    }

    /**
     * Gets a list of caste roles in role form
     * @return a list of caste roles in role form
     */
    private List<Role> getCasteRoles(){
        List<Role> roles = new LinkedList<>();
        for(Role role : guild.getRoles()){
            if(data.getGameConfig().loadSerialized().getRoleIds().contains(role.getIdLong())){
                roles.add(role);
            }
        }
        Comparator<Role> roleComparator = Comparator.comparing(Role::getPosition);
        Collections.sort(roles, roleComparator);
        return roles;
    }

    private int getCasteRoleLevel(Member member){
        Role role = getCasteRoleOfPlayer(member);
        List<Long> ids = data.getGameConfig().loadSerialized().getRoleIds();
        if(role.getIdLong() == data.getGameConfig().loadSerialized().getKingRoleId()){
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
        long kingRoleId = getKingRole().getIdLong();
        for(Role role : member.getRoles()){
            if(roles.contains(role) || kingRoleId == role.getIdLong()){
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
        return guild.getRoleById(data.getGameConfig().loadSerialized().getKingRoleId());
    }

    /**
     * @return war text channel object
     */
    private TextChannel getWarTextChannel(){
        return guild.getTextChannelById(data.getGameConfig().loadSerialized().getGeneralChannelId());
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
                event.reply("Gold must be a posative amount").setEphemeral(true).queue();
                return false;
            }
        }
        if(sc.warChannelOnly()){
            if(event.getChannel().getIdLong() != warChannel.getIdLong()){ // is in war
                event.reply("You can only do this in <#" + warChannel.getIdLong() + ">").setEphemeral(true).queue();
                return false;
            }
        }
        Player player = getAsPlayer(event.getMember());
        if(player.activitiesLeftToday() < sc.activityCheck()){
            event.reply("You do not have enough activities left").setEphemeral(true).queue();
            return false;
        }
        return true;
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

    @SlashCommand(CommandName = "propose-tax", KingOnly = true, activityCheck = 1, warChannelOnly = true, positiveGold = true)
    private void proposeTaxSlashCommand(SlashCommandInteractionEvent event){
        Player king = getAsPlayer(event.getMember());
        Role role = event.getOption("role").getAsRole();
        if(!getCasteRoles().contains(role)){ // is a caste role
            event.reply("You can only tax caste roles").setEphemeral(true).queue();
            return;
        }
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

    @SlashCommand(CommandName = "distribute-wealth", KingOnly = true, positiveGold = true, warChannelOnly = true)
    private void distributeWealthSlashCommand(SlashCommandInteractionEvent event){
        Role role = event.getOption("role").getAsRole();
        if(!getCasteRoles().contains(role)){ // is a caste role
            event.reply("You can only give to caste roles").setEphemeral(true).queue();
            return;
        }
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
        int paddingMultiplier = data.getGameConfig().loadSerialized().getPaddingMultiplier();
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
        if(attackerPoints >= 3){
            // attacker won
            attacker.increaseWins();
            defender.increaseLosses();
            if(attackingUp){
                // attacking up
                goldAmount = r.nextInt(20);
                attacker.increaseGold(defender.payGold(goldAmount));
                swapCasteRoles(attackerMember, defenderMember);
            } else {
                // attacking down
                goldAmount = r.nextInt(20);
                attacker.increaseGold(defender.payGold(goldAmount));
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
                goldAmount = r.nextInt(20);
                defender.increaseGold(attacker.payGold(goldAmount));
            } else {
                // attacking down
                goldAmount = r.nextInt(20);
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
                attackingUp
        );

        attacker.activityCompleted();
        defender.hasDefended();
        data.saveData(attacker, defender);
        resultsData.saveData(cfr);
        data.saveData(data.getGeneral().loadSerialized().increaseChallengesFought());
        event.replyEmbeds(EmbedMessageGenerator.generate(cfr, EmbedMessageGenerator.Detail.SIMPLE)).queue();
    }
}
