package bot.role.data.jsonConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import data.serializing.SaveableData;
import lombok.Getter;

@Getter
public class GameConfigValues extends SaveableData {

    @JsonProperty("server booster padding")
    private int serverBoosterPadding;

    @JsonProperty("start stat max")
    private int startStatMax;

    @JsonProperty("start gold max")
    private int startGoldMax;

    @JsonProperty("activities per day")
    private int activitiesPerDay;

    public GameConfigValues(){
        super("config");
    }
}
