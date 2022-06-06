package bot.role.data;

import bot.role.data.item.ShopItem;
import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.data.structures.*;
import data.serializing.DataCacher;
import data.serializing.SavableData;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

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
    private DataCacher<General> general;

    public Data(){
        players = new DataCacher<>(DIR + "\\players");
        encounters = new DataCacher<>(DIR + "\\encounters");
        activities = new DataCacher<>(DIR +"\\activities");
        guilds = new DataCacher<>(DIR + "\\guilds");
        kingData = new DataCacher<>(DIR + "\\king");
        shopItems = new DataCacher<>(DIR + "\\shop items");
        gameConfig = new DataCacher<>(DIR + "\\game config data");
        general = new DataCacher<>(DIR + "\\general");

        if(general.getFiles().length == 0){
            General g = new General();
            general.saveSerialized(g);
        }

        if(gameConfig.getFiles().length == 0){
            GameConfigValues gcv = new GameConfigValues();
            gcv.setId("Game config");
            gameConfig.saveSerialized(gcv);
        }
    }

    /**
     * Saves a type of data
     * @param data
     */
    public void saveData(SavableData...data){
        for(Field f : Data.class.getDeclaredFields()){
            if(f.getType() != String.class) {
                ParameterizedType dataListType = (ParameterizedType) f.getGenericType();
                Class<?> fieldType = (Class<?>) dataListType.getActualTypeArguments()[0];
                if (data[0].getClass() == fieldType) {
                    try {
                        DataCacher dataCacher = (DataCacher) f.get(this);
                        dataCacher.saveSerialized(data);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
