package bot.role.data.jsonConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import data.serializing.SaveableData;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GameConfigValues extends SaveableData {
    private List<Long> roleIds;
    private long kingRoleId;
    private long guildId;
    private long encountersChannelId;
    private long generalChannelId;
    private long activitiesChannelId;
    private long itemShopChannelId;
    private List<Long> iconIds;
    private long remindMessageId;
    private long swordEmojiId;
    private long fiveGoldEmojiId;
    private long tenGoldEmojiId;
    private long fiftyGoldEmojiId;

    private double activitySpawnChance;
    private int activityLife;

    private double shopItemSpawnChance;
    private int shopItemLife;

    private int startStatMax;
    private int startGoldMax;

    private int serverBoosterPadding;
    private int paddingMultiplier;
    private int activitiesPerDay;
    private int challengeDefendPerDay;

    private int smallDungeonRoomCount;
    private int mediumDungeonRoomCount;
    private int largeDungeonRoomCount;

    public GameConfigValues(){
        iconIds = new LinkedList<>();
        roleIds = new LinkedList<>();
    }
}
