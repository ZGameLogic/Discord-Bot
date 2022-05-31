package bot.role.data;

import bot.role.data.item.Item;
import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Guild extends SaveableData {

    private int guildReputationLevel;
    private Map<Item.Material, Integer> craftingMaterials;
    private int guildReputationLevelXP;
    private int nextGuildLevelThreshold;


    public Guild(String id) {
        super(id);
        guildReputationLevel = 1;
        craftingMaterials = new HashMap<>();
        guildReputationLevelXP = 0;
        nextGuildLevelThreshold = 10;
    }
}
