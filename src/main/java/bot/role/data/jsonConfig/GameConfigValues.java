package bot.role.data.jsonConfig;

import application.App;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GameConfigValues extends SavableData {
    private Map<String, Long> roleIds;
    private Map<String, Long> iconIds;
    private Map<String, Long> channelIds;
    private long warCategoryId;
    private long guildsCategoryId;
    private long guildId;

    private int goldTaxMax;

    private double activitySpawnChance;
    private int activityLife;

    private double shopItemSpawnChance;
    private int shopItemLife;

    private double encounterSpawnChance;
    private int encounterLife;

    private double tournamentSpawnChance;
    private int tournamentLife;

    private double dungeonSpawnChance;
    private int dungeonLife;

    private int startStatMax;
    private int startGoldMax;

    private int serverBoosterPadding;
    private double paddingMultiplier;
    private int activitiesPerDay;
    private int challengeDefendPerDay;

    private int smallDungeonRoomCount;
    private int mediumDungeonRoomCount;
    private int largeDungeonRoomCount;

    public GameConfigValues(){
        super("Game config");
        iconIds = new HashMap<>();
        roleIds = new HashMap<>();
        channelIds = new HashMap<>();
        goldTaxMax = 7;
        activitySpawnChance = .1;
        activityLife = 4;
        shopItemSpawnChance = .2;
        shopItemLife = 4;
        encounterSpawnChance = .15;
        encounterLife = 4;
        dungeonSpawnChance = .15;
        dungeonLife = 6;
        startGoldMax = 30;
        startStatMax = 15;
        paddingMultiplier = .25;
        activitiesPerDay = 3;
        challengeDefendPerDay = 3;
        smallDungeonRoomCount = 20;
        mediumDungeonRoomCount = 25;
        largeDungeonRoomCount = 30;
        guildId = App.config.getGuildID();
    }
}
