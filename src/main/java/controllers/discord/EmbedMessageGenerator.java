package controllers.discord;

import bot.role.data.Data;
import bot.role.data.dungeon.saveable.Dungeon;
import bot.role.data.structures.Tournament;
import bot.role.data.structures.item.Item;
import bot.role.data.structures.item.Modifier;
import bot.role.data.structures.item.ShopItem;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.results.*;
import bot.role.data.structures.Activity;
import bot.role.data.structures.Encounter;
import bot.role.data.structures.Player;
import bot.role.helpers.DungeonGenerator;
import bot.role.helpers.roleData.RoleDataRepository;
import controllers.minecraft.structures.MinecraftServer;
import controllers.pokemon.structures.Pokemon;
import data.serializing.DataRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.Function;

/**
 * This class automatically generates embedded messages
 * @author Ben Shabowski
 * @since 1.0
 */
public abstract class EmbedMessageGenerator {

    private static DataRepository<Strings> strings = new DataRepository<Strings>("arena\\strings");

    private final static Color ACTIVITY_COLOR = new Color(175, 102, 45);
    private final static Color NEW_DAY_COLOR = new Color(118, 21, 161);
    private final static Color CHALLENGE_WIN_COLOR = new Color(25, 83, 44);
    private final static Color CHALLENGE_LOSE_COLOR = new Color(83, 25, 26);
    private final static Color KING_MESSAGE_COLOR = new Color(250, 208, 4);
    private final static Color STATS_COLOR = new Color(112, 93, 115);
    private final static Color LEADERBOARD_COLOR = new Color(101, 106, 15);
    private final static Color ENCOUNTER_COLOR = new Color(56, 78, 115);
    private final static Color PAY_CITIZEN_COLOR = new Color(150, 58, 58);
    private final static Color REMIND_MESSAGE_COLOR = new Color(64, 140, 147);
    private final static Color TOURNAMENT_COLOR = new Color(80, 21, 108);
    private final static Color GUILD_COLOR = new Color(87, 70, 14);

    private final static Color SMALL_DUNGEON_COLOR = new Color(118, 191, 38, 255);
    private final static Color MEDIUM_DUNGEON_COLOR = new Color(59, 94, 21);
    private final static Color LARGE_DUNGEON_COLOR = new Color(29, 47, 10);

    private final static Color MYTHIC_ITEM_COLOR = new Color(248, 29, 1);
    private final static Color LEGENDARY_ITEM_COLOR = new Color(248, 170, 1);
    private final static Color EPIC_ITEM_COLOR = new Color(248, 235, 1);
    private final static Color RARE_ITEM_COLOR = new Color(0, 248, 241);
    private final static Color UNCOMMON_ITEM_COLOR = new Color(40, 184, 180);
    private final static Color COMMON_ITEM_COLOR = new Color(67, 144, 143);

    private final static Color MINECRAFT_SERVER_COLOR = new Color(130, 179, 98);

    private final static Color ATLASSIAN_COLOR = new Color(7, 70, 166);

    public enum Detail {
        SIMPLE,
        COMPLEX
    }

    public static MessageEmbed generateJiraVersion(String versionName, String versionDescription){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle(versionName + " update has been released!");
        eb.setDescription(versionDescription);
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed generateGuildJoinRequest(Member member){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(GUILD_COLOR);
        b.setTitle("A player has requested to join the guild!");
        b.setDescription(member.getEffectiveName());
        b.setThumbnail(member.getAvatarUrl());
        b.setFooter(member.getId());
        return b.build();
    }

    public static MessageEmbed generate(MinecraftServer mcServer){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(MINECRAFT_SERVER_COLOR);
        if(mcServer.isOnline()){
            b.setTitle("Server status of: " + mcServer.getServerName());
            b.setFooter("Online: " + mcServer.isOnline());
            b.addField("Player count", mcServer.getPlayerCount() + "", true);
            if(mcServer.getPlayerCount() > 0)
                b.addField("Online players", mcServer.playerListString(), true);
        } else {
            b.setTitle("This server is offline");
        }
        return b.build();
    }

    public static MessageEmbed generateRoleStat(List<Player> players, String roleName, String icon){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(STATS_COLOR);
        b.setTitle("Player stats for the " + roleName);
        if(!icon.equals("")) b.setImage(icon);
        b.setDescription("Stats are encoded as Agility | Strength | Stamina | Knowledge | Magic Gold Wins/Losses");
        for(Player p : players){
            b.addField(p.getName(), p.getCompactStatLine(), true);
        }
        return b.build();
    }

    public static MessageEmbed generateRemindMessage(){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(REMIND_MESSAGE_COLOR);
        b.setTitle("A message to all those who like to forget");
        b.setDescription("Do not forget to do your activities for the day!");
        b.setFooter("From your lord and savior, Shlongbot");
        b.setTimestamp(Instant.now());
        return b.build();
    }

    /**
     * Generates a Message embed for a king player
     * @param king King member
     * @return a built message embed for the king
     */
    public static MessageEmbed generateNewKing(Member king) {
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(KING_MESSAGE_COLOR);
        b.setTitle("Hail to the new ruler: " + king.getEffectiveName() + "!");
        b.setTimestamp(Instant.now());
        return b.build();
    }

    public static MessageEmbed generate(Pokemon pokemon){
        EmbedBuilder b = new EmbedBuilder();
        b.setTitle(pokemon.getName());
        b.setColor(pokemon.getTypes().get(0).getColor());
        b.setDescription(pokemon.getPokedexEntry());
        b.setThumbnail(pokemon.getSprite());
        b.setFooter("Pokedex index: " + pokemon.getPokedexId());
        String typeString = "";
        for(Pokemon.Type type : pokemon.getTypes()){
            typeString += type + " ";
        }
        b.addField("Type", typeString, true);
        typeString = "";
        for(Pokemon.Type type : pokemon.getWeakTo()){
            typeString += type + " ";
        }
        b.addField("Weak to", typeString, true);
        String abilities = "";
        for(Pokemon.Ability ability : pokemon.getAbilities()){
            abilities += ability.isHidden() ? "*" + ability.getName() + "*, " : ability.getName() + ", ";
        }
        abilities = abilities.substring(0, abilities.length() - 2);
        b.addField("Abilities", abilities, false);

        b.addField("Base stats",
                "Attack " + pokemon.getBaseAttack() + "\n" +
                        "Defense " + pokemon.getBaseDefense() + "\n" +
                        "Special Attack " + pokemon.getBaseSpecialAttack() + "\n" +
                        "Special Defense " + pokemon.getBaseSpecialDefense() + "\n" +
                        "Speed " + pokemon.getBaseSpeed(),
                false);

        return b.build();
    }

    public static MessageEmbed generateLeaderboard(Function<Player, Integer> function, String title, Data data) {
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(LEADERBOARD_COLOR);
        b.setTitle("Leaderboard for " + title);
        List<Player> players = data.getPlayers().getData();
        Collections.sort(players, Comparator.comparing(function).reversed());
        Map<String, String> roles = RoleDataRepository.getRoleMap();
        for(int i = 0; i < 10 && i < players.size(); i++){
            Player p = players.get(i);
            String iconStringId = roles.get(p.getCasteLevel());
            long iconLongId = data.getGameConfig().loadSerialized().getIconIds().get(iconStringId);
            String embeddedIcon = "<:" + iconStringId + ":" + iconLongId + ">";
            b.addField(p.getName() + " " + embeddedIcon, function.apply(p) + "", true);
        }
        return b.build();
    }

    public static MessageEmbed generate(Tournament tournament) {
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(TOURNAMENT_COLOR);
        b.setTitle(tournament.getName());
        b.setTimestamp(tournament.getDeparts().toInstant());
        b.setFooter("");
        b.addField("Entry fee", tournament.getEntryFee() + " gold", true);
        return b.build();
    }

    public static MessageEmbed generate(Dungeon dungeon) {
        EmbedBuilder b = new EmbedBuilder();
        switch(dungeon.getSize()){
            case SMALL:
                b.setColor(SMALL_DUNGEON_COLOR);
                break;
            case MEDIUM:
                b.setColor(MEDIUM_DUNGEON_COLOR);
                break;
            case LARGE:
                b.setColor(LARGE_DUNGEON_COLOR);
                break;
        }
        b.setTitle("Dungeon of DEATH"); // TODO make fun titles
        b.setImage("attachment://dungeon.png");
        b.setFooter("Closes");
        b.setTimestamp(dungeon.getDeparts().toInstant());
        return b.build();
    }

    public static MessageEmbed generateStatsMessage(Player player, Member member){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(STATS_COLOR);
        b.setTitle("Fighter stats for " + member.getEffectiveName());
        b.setThumbnail(member.getAvatarUrl());
        b.setDescription("Gold: " + player.getGold() + "\n" +
                "Victories: " + player.getWins() + "\n" +
                        "Defeats: " + player.getLosses() + "\n" +
                "Tournament victories: " + player.getTournamentVictories());

        b.addField("Strength", player.getStrengthToStringWithItem(), true);
        b.addField("Knowledge", player.getKnowledgeToStringWithItem(), true);
        b.addField("Magic", player.getMagicToStringWithItem(), true);
        b.addField("Agility", player.getAgilityToStringWithItem(), true);
        b.addField("Stamina", player.getStaminaToStringWithItem(), true);

        b.addBlankField(false);

        int slot = 1;
        for(Item item : player.getInventory()) {
            if (item != null) {
                String desc = item.getDescription() + "\n";
                for (Modifier mod : item.getModifiers()) {
                    if (mod.getType() == Modifier.Type.BANE) {
                        desc += "\t" + mod.getType().getString() + " " + mod.getStat().getString() + "\n";
                    } else {
                        desc += "\t" + mod.getType().getString() + " " + mod.getStat().getString() + ": " + mod.getAmount() + "\n";
                    }
                }
                desc += "Slot: " + slot + "\n";
                b.addField(item.getName(), desc, true);
            }
            slot++;
        }

        b.setFooter("Can do " + player.activitiesLeftToday() + " more activities today\n" +
                "Can defend " + player.defendsLeftToday() + " more times today");
        return b.build();
    }

    public static MessageEmbed generateProposeTaxMessage(Player player, Role role, int gold){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(KING_MESSAGE_COLOR);
        b.setTitle(player.getName() + " has proposed a tax on the " + role.getName() + "!");
        b.setDescription("Gold amount: " + gold);
        b.setTimestamp(Instant.now());
        return b.build();
    }

    public static MessageEmbed generateDistributeWealth(Player giver, int gold, Role role){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(KING_MESSAGE_COLOR);
        b.setTitle(giver.getName() + ", our glorious ruler, has given " + gold + " gold to the " + role.getName() + " caste!");
        b.setTimestamp(Instant.now());
        return b.build();
    }

    public static MessageEmbed generatePayCitizen(Player giver, Player taker, int gold){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(PAY_CITIZEN_COLOR);
        b.setTitle(giver.getName() + " has given " + gold + " gold to " + taker.getName());
        b.setTimestamp(Instant.now());
        return b.build();
    }

    /**
     * @return A new day message
     */
    public static MessageEmbed generateNewDay(int dayCount){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(NEW_DAY_COLOR);
        b.setTitle("A message from the current ruler of Shlongshot");
        b.setDescription(strings.loadSerialized().getDayMessageStart() + " " + strings.loadSerialized().getDayMessageEnd());
        b.setFooter("Day count: " + dayCount);
        return b.build();
    }

    public static MessageEmbed generate(ActivityResults results) {
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(ACTIVITY_COLOR);
        b.setTitle("Activity results for " + results.getPlayerName());
        b.addField(results.getReward(), results.getRewardAmount() + "", true);
        b.setTimestamp(results.getTime().toInstant());
        return b.build();
    }

    public static MessageEmbed generate(ChallengeFightResults results, Detail detail){
        switch (detail){
            case SIMPLE:
                return generateSimpleFightResult(results);
            case COMPLEX:
                return generateComplexFightResult(results);
        }
        return new EmbedBuilder().build();
    }

    private static MessageEmbed generateSimpleFightResult(ChallengeFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        String description = "";
        String attackerName = results.getAttacker().getName();
        String defenderName = results.getDefender().getName();
        description += attackerName + " vs " + defenderName + "\n";
        if(results.isAttackerWin()){
            b.setColor(CHALLENGE_WIN_COLOR);
            b.setTitle("Fight results: " + results.getAttacker().getName() + " won!");
            description += attackerName + " is now a rank of " + results.getDefenderRole() + "." +
                    " Gold obtained: " + results.getGold();
        } else {
            b.setColor(CHALLENGE_LOSE_COLOR);
            description += "Better luck next time. " + results.getResultStatChange() + "\nGold lost: " + results.getGold() + ".";
            b.setTitle("Fight results: " + results.getAttacker().getName() + " lost");
        }
        if(results.isBounty()) description += "\n" + defenderName + " had a bounty on them! Bounty: " + results.getBountyAmount() + " gold.";
        b.setDescription(description);
        b.addField("Fight statistics", "Attacker points: " + results.getAttackerPoints() + "\n" +
                "Defender points: " + (5 - results.getAttackerPoints()), true);
        b.setTimestamp(results.getTime().toInstant());
        b.setFooter(results.getId());
        return b.build();
    }

    private static MessageEmbed generateComplexFightResult(ChallengeFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        String attackerName = results.getAttacker().getName();
        String defenderName = results.getDefender().getName();
        String description = "";
        if(results.isAttackerWin()){
            b.setColor(CHALLENGE_WIN_COLOR);
        } else {
            b.setColor(CHALLENGE_LOSE_COLOR);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        description += "Fight time: " + sdf.format(results.getTime()) +"\n" +
                "Fight between " + attackerName + " (" + results.getAttackerRole() + ") and " + defenderName + " (" + results.getDefenderRole() + ")\n" +
                "Defender bounty: " + (results.isBounty() ? results.getBountyAmount() : false) + "\n" +
                "Attacker win: " + results.isAttackerWin() + "\n" +
                "Score: " + results.getAttackerPoints() + " " + (5 - results.getAttackerPoints()) + "\n" +
                "Padding Multiplier: x" + results.getPaddingMultiplier() + "\n" +
                "Defender padding level: " + results.getDefenderPaddingLevel() + "\n" +
                "Attacker win percentage: " + String.format("%.2f%%", results.attackerWinPercentage()) + "\n\n";
        for(String stat : results.getAttacker().getStatBlockWithItems().getAllStats().keySet()){
            int attackerStat = results.getAttacker().getStatBlockWithItems().getAllStats().get(stat);
            int defenderStat = results.getDefender().getStatBlockWithItems().getAllStats().get(stat);
            int rolled = results.getRolled().getAllStats().get(stat);
            int total = attackerStat + defenderStat;
            double winPercentage = ((double) attackerStat / total) * 100;
            b.addField(stat, String.format("%.2f", winPercentage) + "%: " + (rolled <= attackerStat ? "won" : "lost") + "\n" +
                    "\tA: " + attackerStat + "\tD: " + defenderStat + "\n" +
                    "\tTotal: " + total + "\tRolled: " + rolled, true);
        }
        b.setDescription(description);
        b.setFooter(results.getId());
        return b.build();
    }

    public static MessageEmbed generate(EncounterFightResults results, Detail detail){
        switch (detail){
            case SIMPLE:
                return generateSimpleEncounterResults(results);
            case COMPLEX:
                return generateComplexEncounterResults(results);
        }
        return new EmbedBuilder().build();
    }

    private static MessageEmbed generateSimpleEncounterResults(EncounterFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        b.setTitle("Encounter fight results for " + results.getPlayer().getName());
        String description = "";
        if(results.isAttackerWon()){
            b.setColor(CHALLENGE_WIN_COLOR);
            description += "You have won the fight against a " + results.getEncounterName() + "\n" +
                    "Gold gained: " + results.getGold() + " " + results.getResultStatChange();
        } else {
            b.setColor(CHALLENGE_LOSE_COLOR);
            description += "You have lost the fight against a " + results.getEncounterName() + "\n" +
                    "Gold lost: " + results.getGold();
        }
        b.setDescription(description);
        b.setTimestamp(results.getTime().toInstant());
        b.setFooter(results.getId());
        return b.build();
    }

    private static MessageEmbed generateComplexEncounterResults(EncounterFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(GuildFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(GuildRaidResults results){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(ItemCraftingResults results){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(ItemPurchaseResults results){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(TournamentFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(TournamentResults results){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(ShopItem shopItem){
        EmbedBuilder b = new EmbedBuilder();
        Item item = shopItem.getItem();
        switch(item.getRarity()){
            case MYTHIC:
                b.setColor(MYTHIC_ITEM_COLOR);
                break;
            case LEGENDARY:
                b.setColor(LEGENDARY_ITEM_COLOR);
                break;
            case EPIC:
                b.setColor(EPIC_ITEM_COLOR);
                break;
            case RARE:
                b.setColor(RARE_ITEM_COLOR);
                break;
            case UNCOMMON:
                b.setColor(UNCOMMON_ITEM_COLOR);
                break;
            case COMMON:
                b.setColor(COMMON_ITEM_COLOR);
                break;
        }
        b.setAuthor(item.getRarity().getString());
        b.setTitle(item.getName() + " is in stock today!");
        b.setDescription(item.getDescription());
        int effectCount = 1;
        for(Modifier m : item.getModifiers()){
            String desc = m.getType() == Modifier.Type.BANE ? "Bane " + m.getStat().getString() :
                    m.getType().getString() + " " + m.getStat().getString() + " increase by: " + m.getAmount();
            b.addField("Effect " + effectCount++, desc, true);
        }
        b.addField("Cost", shopItem.getGoldCost() + " gold", false);
        b.setFooter("Expires");
        b.setTimestamp(shopItem.getDeparts().toInstant());
        return b.build();
    }
    public static MessageEmbed generate(Activity activity){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(ACTIVITY_COLOR);
        switch(activity.getType()){
            case JOB:
                b.setTitle("A job had been posted on the city board: " + activity.getActivityName());
                b.addField("Activity cost", activity.getActivityCost() + "", true);
                b.addField("Reward", activity.getGold() + " gold", true);
                break;
            case TRAINING:
                b.setTitle(activity.getActivityName() + " has posted on the city board: " + activity.getStatType() + " training!");
                b.addField("Activity cost", activity.getActivityCost() + "", true);
                b.addField("Gold cost", activity.getGold() + " gold", true);
                b.addField("Reward", activity.getStatAmount() + " " + activity.getStatType(), true);
                break;
        }
        b.setFooter("Departs");
        b.setTimestamp(activity.getDeparts().toInstant());
        return b.build();
    }
    public static MessageEmbed generate(Encounter encounter){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(ENCOUNTER_COLOR);
        b.setTitle("A " + encounter.getName() + " challenges the kingdom!");
        encounter.getStatBlock().getAllStats().forEach((key, value) -> {
            b.addField(key, value + "", true);
        });
        b.setFooter("departs");
        b.setTimestamp(encounter.getDeparts().toInstant());
        return b.build();
    }
    public static MessageEmbed generateRollSwap(Player player1, Player player2){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(KING_MESSAGE_COLOR);
        b.setTitle("The ruler of shlongshot has decreed a role change!");
        b.setDescription("By the command of our king/queen: " + player1.getName() + " and " + player2.getName() + " have had their roles swapped!");
        return b.build();
    }
}
