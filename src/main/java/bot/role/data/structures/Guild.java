package bot.role.data.structures;

import bot.role.data.item.Item;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Guild extends SavableData {

    private int guildReputationLevel;
    private Map<Item.Material, Integer> craftingMaterials;
    private int guildReputationLevelXP;
    private int nextGuildLevelThreshold;
    private long textChannelId;
    private long voiceChannelId;


    public Guild(String id, long textChannelId, long voiceChannelId) {
        super(id);
        guildReputationLevel = 1;
        craftingMaterials = new HashMap<>();
        guildReputationLevelXP = 0;
        nextGuildLevelThreshold = 10;
        this.textChannelId = textChannelId;
        this.voiceChannelId = voiceChannelId;
    }
}
