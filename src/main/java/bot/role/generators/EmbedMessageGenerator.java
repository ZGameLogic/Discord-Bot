package bot.role.generators;

import bot.role.data.jsonConfig.Strings;
import bot.role.data.results.*;
import data.serializing.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public abstract class EmbedMessageGenerator {

    private static DataCacher<Strings> strings = new DataCacher<Strings>("arena\\strings");

    private static Color ACTIVITY_COLOR = new Color(175, 102, 45);
    private static Color NEW_DAY_COLOR = new Color(118, 21, 161);
    private static Color CHALLENGE_WIN_COLOR = new Color(25, 83, 44);
    private static Color CHALLENGE_LOSE_COLOR = new Color(83, 25, 26);
    private static Color KING_MESSAGE = new Color(250, 208, 4);
    private static Color STATS_COLOR = new Color(112, 93, 115);
    private static Color LEADERBOARD_COLOR = new Color(101, 106, 15);

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
        return null;
    }

    private static MessageEmbed generateSimpleFightResult(ChallengeFightResults results){
        EmbedBuilder b = new EmbedBuilder();
        String description = "";
        if(results.isAttackerWin()){
            b.setColor(CHALLENGE_WIN_COLOR);
            b.setTitle("Fight results: " + results.getAttacker().getName() + " won!");
        } else {
            b.setColor(CHALLENGE_LOSE_COLOR);
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

        return b.build();
    }


    public static MessageEmbed generate(EncounterFightResults results){
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
}
