package bot.role.data;

import bot.role.data.item.ShopItem;
import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.data.structures.*;
import data.serializing.DataCacher;
import lombok.Getter;

@Getter
public class Data {
    private static final String DIR = "arena";

    private DataCacher<Player> players;
    private DataCacher<Encounter> encounters;
    private DataCacher<Activity> activities;
    private DataCacher<ShopItem> shopItems;
    private DataCacher<Guild> guilds;
    private DataCacher<KingData> kingData;
    private DataCacher<GameConfigValues> gameConfig;

    public Data(){
        players = new DataCacher<>(DIR + "\\players");
        encounters = new DataCacher<>(DIR + "\\encounters");
        activities = new DataCacher<>(DIR +"\\activities");
        guilds = new DataCacher<>(DIR + "\\guilds");
        kingData = new DataCacher<>(DIR + "\\king");
        shopItems = new DataCacher<>(DIR + "\\shop items");
        gameConfig = new DataCacher<>(DIR + "\\game config data");

        if(gameConfig.getFiles().length == 0){
            GameConfigValues gcv = new GameConfigValues();
            gcv.setId("Game config");
            gameConfig.saveSerialized(gcv);
        }
    }
}
