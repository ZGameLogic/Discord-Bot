package bot.role.generators;

import bot.role.data.item.ShopItem;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.results.*;
import bot.role.data.structures.Encounter;
import bot.role.data.structures.Player;
import data.serializing.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public abstract class EmbedMessageGenerator {

    private static DataCacher<Strings> strings = new DataCacher<Strings>("arena\\strings");

    private static Color ACTIVITY_COLOR = new Color(175, 102, 45);
    private static Color NEW_DAY_COLOR = new Color(118, 21, 161);
    private static Color CHALLENGE_WIN_COLOR = new Color(25, 83, 44);
    private static Color CHALLENGE_LOSE_COLOR = new Color(83, 25, 26);
    private static Color KING_MESSAGE_COLOR = new Color(250, 208, 4);
    private static Color STATS_COLOR = new Color(112, 93, 115);
    private static Color LEADERBOARD_COLOR = new Color(101, 106, 15);

    private static Color MYTHIC_ITEM_COLOR = new Color(248, 29, 1);
    private static Color LEGENDARY_ITEM_COLOR = new Color(248, 170, 1);
    private static Color EPIC_ITEM_COLOR = new Color(248, 235, 1);
    private static Color RARE_ITEM_COLOR = new Color(0, 248, 241);
    private static Color UNCOMMON_ITEM_COLOR = new Color(40, 184, 180);
    private static Color COMMON_ITEM_COLOR = new Color(67, 144, 143);



    public enum Detail {
        SIMPLE,
        COMPLEX
    }

    /**
     * @return A new day message
     */
    public static MessageEmbed generateNewDay(){
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(NEW_DAY_COLOR);
        b.setTitle("A message from teh current ruler of Shlongshot");
        b.setDescription(strings.loadSerialized().getDayMessageStart() + " " + strings.loadSerialized().getDayMessageEnd());
        return b.build();
    }

    public static MessageEmbed generate(ActivityResults results) {
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(ACTIVITY_COLOR);
        b.setTitle("Activity results for " + results.getPlayerName());
        b.addField(results.getReward(), results.getRewardAmount() + "", true);
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
            description += attackerName + " is now a rank of " + results.getResultStatChange() + "." +
                    " Gold obtained: " + results.getGold();
        } else {
            b.setColor(CHALLENGE_LOSE_COLOR);
            description += "Better luck next time. " + results.getResultStatChange() + "\nGold lost: " + results.getGold() + ".";
            b.setTitle("Fight results: " + results.getAttacker().getName() + " lost");
        }
        b.setDescription(description);
        b.addField("Fight statistics", "Attacker points: " + results.getAttackerPoints() + "\n" +
                "Defender points: " + (5 - results.getAttackerPoints()), true);
        b.setFooter(results.getId());
        return b.build();
    }

    private static MessageEmbed generateComplexFightResult(ChallengeFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        // TODO this
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
            // TODO add description
        } else {
            b.setColor(CHALLENGE_LOSE_COLOR);
            // TODO add description
        }
        b.setDescription(description);
        b.setFooter(results.getId());
        return b.build();
    }

    private static MessageEmbed generateComplexEncounterResults(EncounterFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        // TODO this
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
    public static MessageEmbed generate(ShopItem item){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(Activity activity){
        EmbedBuilder b = new EmbedBuilder();
        return b.build();
    }
    public static MessageEmbed generate(Encounter encounter){
        EmbedBuilder b = new EmbedBuilder();
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
