package bot.role.data;

import data.serializing.SaveableData;
import lombok.Getter;

@Getter
public class Guild extends SaveableData {

    private int guildLevel;

    public Guild(String id) {
        super(id);
        guildLevel = 1;
    }
}
